#!/usr/bin/env node
// Genera el sitio estático de documentación a partir de los .md de docs/.
//
// Uso:  node docs/generate.mjs
//
// - Convierte cada docs/*.md en docs/site/<nombre>.html
// - Genera docs/site/index.html con el índice de boletines
// - Copia las imágenes (y demás recursos) respetando las subcarpetas
//
// No tiene dependencias externas: usa solo módulos de Node.

import { readdir, readFile, writeFile, mkdir, rm, copyFile } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";

const docsDir = path.dirname(fileURLToPath(import.meta.url));
const siteDir = path.join(docsDir, "site");
const ASSET_EXTENSIONS = new Set([".png", ".jpg", ".jpeg", ".gif", ".svg", ".webp", ".pdf"]);

/* ------------------------------------------------------------------ */
/* Utilidades                                                          */
/* ------------------------------------------------------------------ */

const escapeHtml = (text) =>
  text
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");

const escapeAttr = escapeHtml;

const slugify = (text) =>
  text
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "");

/* ------------------------------------------------------------------ */
/* Markdown en línea                                                   */
/* ------------------------------------------------------------------ */

function inline(text) {
  const codes = [];

  // 1. Código en línea: se protege antes de escapar el resto.
  text = text.replace(/`([^`]+)`/g, (_, code) => {
    codes.push(`<code>${escapeHtml(code)}</code>`);
    return `\u0000${codes.length - 1}\u0000`;
  });

  text = escapeHtml(text);

  // 2. Imágenes (antes que los enlaces).
  text = text.replace(/!\[([^\]]*)\]\(([^\s)]+)\)/g, (_, alt, src) => {
    return `<img src="${escapeAttr(src)}" alt="${escapeAttr(alt)}" title="${escapeAttr(alt)}">`;
  });

  // 3. Enlaces.
  text = text.replace(/\[([^\]]+)\]\(([^\s)]+)\)/g, (_, label, href) => {
    const external = /^https?:\/\//.test(href);
    const target = external ? ' target="_blank" rel="noopener noreferrer"' : "";
    return `<a href="${escapeAttr(href)}"${target}>${label}</a>`;
  });

  // 4. Énfasis.
  text = text.replace(/\*\*([^*]+)\*\*/g, "<strong>$1</strong>");
  text = text.replace(/__([^_]+)__/g, "<strong>$1</strong>");
  text = text.replace(/~~([^~]+)~~/g, "<del>$1</del>");
  text = text.replace(/(^|[^*])\*([^*\n]+)\*/g, "$1<em>$2</em>");
  text = text.replace(/(^|[^_\w])_([^_\n]+)_(?![\w])/g, "$1<em>$2</em>");

  // 5. Se restauran los fragmentos de código.
  return text.replace(/\u0000(\d+)\u0000/g, (_, index) => codes[Number(index)]);
}

/* ------------------------------------------------------------------ */
/* Markdown en bloque                                                  */
/* ------------------------------------------------------------------ */

const isBlank = (line) => /^\s*$/.test(line);

const startsBlock = (line) =>
  /^\s*(```|~~~)/.test(line) ||
  /^#{1,6}\s+/.test(line) ||
  /^\s*>/.test(line) ||
  /^\s*([-*+]|\d+[.)])\s+/.test(line) ||
  /^\s*([-*_])\s*(\1\s*){2,}$/.test(line);

const isTableDelimiter = (line) =>
  /^\s*\|?\s*:?-{3,}:?\s*(\|\s*:?-{3,}:?\s*)+\|?\s*$/.test(line);

function splitTableRow(line) {
  const trimmed = line.trim().replace(/^\|/, "").replace(/\|$/, "");
  const cells = [];
  let current = "";
  let inCode = false;

  for (let i = 0; i < trimmed.length; i += 1) {
    const char = trimmed[i];
    if (char === "\\" && trimmed[i + 1] === "|") {
      current += "|";
      i += 1;
      continue;
    }
    if (char === "`") inCode = !inCode;
    if (char === "|" && !inCode) {
      cells.push(current.trim());
      current = "";
      continue;
    }
    current += char;
  }

  cells.push(current.trim());
  return cells;
}

function parseList(lines, start) {
  const first = lines[start].match(/^(\s*)([-*+]|\d+[.)])\s+(.*)$/);
  const baseIndent = first[1].length;
  const ordered = /\d/.test(first[2]);
  const startNumber = ordered ? Number.parseInt(first[2], 10) : 1;
  const items = [];
  let i = start;

  const sameListMarker = (line) => {
    const match = line.match(/^(\s*)([-*+]|\d+[.)])\s+/);
    return match && match[1].length === baseIndent && /\d/.test(match[2]) === ordered;
  };

  while (i < lines.length) {
    // Línea en blanco entre elementos de la misma lista.
    if (isBlank(lines[i])) {
      let next = i + 1;
      while (next < lines.length && isBlank(lines[next])) next += 1;
      if (next < lines.length && sameListMarker(lines[next])) {
        i = next;
        continue;
      }
      break;
    }

    const match = lines[i].match(/^(\s*)([-*+]|\d+[.)])\s+(.*)$/);
    if (!match || !sameListMarker(lines[i])) break;

    const markerIndent = match[1].length;
    const contentIndent = match[0].length - match[3].length;
    const itemLines = [match[3]];
    i += 1;

    while (i < lines.length) {
      const line = lines[i];

      if (isBlank(line)) {
        let next = i + 1;
        while (next < lines.length && isBlank(lines[next])) next += 1;
        if (next >= lines.length) break;
        const nextIndent = lines[next].match(/^\s*/)[0].length;
        if (sameListMarker(lines[next])) break;
        if (nextIndent > markerIndent) {
          itemLines.push("");
          i += 1;
          continue;
        }
        break;
      }

      const indent = line.match(/^\s*/)[0].length;
      if (indent > markerIndent) {
        itemLines.push(line);
        i += 1;
        continue;
      }
      break;
    }

    const dedented = itemLines.map((line) =>
      line.slice(Math.min(contentIndent, line.match(/^\s*/)[0].length)),
    );
    items.push(parseBlocks(dedented));
  }

  const tag = ordered ? "ol" : "ul";
  const startAttr = ordered && startNumber !== 1 ? ` start="${startNumber}"` : "";
  const html = `<${tag}${startAttr}>\n${items
    .map((item) => `<li>${item}</li>`)
    .join("\n")}\n</${tag}>`;

  return [html, i];
}

function parseTable(lines, start) {
  const header = splitTableRow(lines[start]);
  const aligns = splitTableRow(lines[start + 1]).map((cell) => {
    const left = cell.startsWith(":");
    const right = cell.endsWith(":");
    if (left && right) return "center";
    if (right) return "right";
    if (left) return "left";
    return "";
  });

  const style = (index) => {
    const align = aligns[index];
    return align ? ` style="text-align: ${align}"` : "";
  };

  const head = `<thead><tr>${header
    .map((cell, index) => `<th${style(index)}>${inline(cell)}</th>`)
    .join("")}</tr></thead>`;

  let i = start + 2;
  const rows = [];
  while (i < lines.length && !isBlank(lines[i]) && lines[i].includes("|")) {
    if (startsBlock(lines[i]) && !lines[i].trim().startsWith("|")) break;
    const cells = splitTableRow(lines[i]);
    rows.push(
      `<tr>${cells
        .map((cell, index) => `<td${style(index)}>${inline(cell)}</td>`)
        .join("")}</tr>`,
    );
    i += 1;
  }

  const body = rows.length ? `<tbody>${rows.join("")}</tbody>` : "";
  return [`<div class="table-wrapper"><table>${head}${body}</table></div>`, i];
}

function parseBlocks(lines) {
  const out = [];
  let i = 0;

  while (i < lines.length) {
    const line = lines[i];

    if (isBlank(line)) {
      i += 1;
      continue;
    }

    // Bloques de código.
    const fence = line.match(/^\s*(`{3,}|~{3,})\s*([\w+#.-]*)\s*$/);
    if (fence) {
      const marker = fence[1][0];
      const length = fence[1].length;
      const closing = new RegExp(`^\\s*${marker}{${length},}\\s*$`);
      const body = [];
      i += 1;
      while (i < lines.length && !closing.test(lines[i])) {
        body.push(lines[i]);
        i += 1;
      }
      i += 1; // salta el cierre
      const language = fence[2] ? ` class="language-${escapeAttr(fence[2])}"` : "";
      out.push(`<pre><code${language}>${escapeHtml(body.join("\n"))}</code></pre>`);
      continue;
    }

    // Encabezados.
    const heading = line.match(/^(#{1,6})\s+(.*?)\s*#*\s*$/);
    if (heading) {
      const level = heading[1].length;
      out.push(`<h${level} id="${slugify(heading[2])}">${inline(heading[2])}</h${level}>`);
      i += 1;
      continue;
    }

    // Separador horizontal.
    if (/^\s*([-*_])\s*(\1\s*){2,}$/.test(line)) {
      out.push("<hr>");
      i += 1;
      continue;
    }

    // Cita.
    if (/^\s*>/.test(line)) {
      const body = [];
      while (i < lines.length && /^\s*>/.test(lines[i])) {
        body.push(lines[i].replace(/^\s*>\s?/, ""));
        i += 1;
      }
      out.push(`<blockquote>${parseBlocks(body)}</blockquote>`);
      continue;
    }

    // Tabla.
    if (
      line.includes("|") &&
      i + 1 < lines.length &&
      isTableDelimiter(lines[i + 1]) &&
      splitTableRow(line).length === splitTableRow(lines[i + 1]).length
    ) {
      const [html, next] = parseTable(lines, i);
      out.push(html);
      i = next;
      continue;
    }

    // Lista.
    if (/^\s*([-*+]|\d+[.)])\s+/.test(line)) {
      const [html, next] = parseList(lines, i);
      out.push(html);
      i = next;
      continue;
    }

    // Párrafo.
    const paragraph = [];
    while (i < lines.length && !isBlank(lines[i]) && !startsBlock(lines[i])) {
      paragraph.push(lines[i].trim());
      i += 1;
    }
    if (paragraph.length) {
      const text = paragraph
        .join("\n")
        .replace(/ {2,}\n/g, "<br>\n");
      out.push(`<p>${inline(text)}</p>`);
    }
  }

  return out.join("\n");
}

function markdownToHtml(markdown) {
  const cleaned = markdown
    .replace(/<!--[\s\S]*?-->/g, "")
    .replace(/\r\n?/g, "\n")
    .replace(/\t/g, "    ");
  return parseBlocks(cleaned.split("\n"));
}

/* ------------------------------------------------------------------ */
/* Plantilla HTML                                                      */
/* ------------------------------------------------------------------ */

const styles = `
  :root {
    --bg: #f6f7fb;
    --surface: #ffffff;
    --text: #1f2430;
    --muted: #5b6474;
    --accent: #2563eb;
    --accent-soft: #dbeafe;
    --border: #e2e6ef;
    --code-bg: #0f172a;
    --code-text: #e2e8f0;
    --inline-code: #eef2ff;
  }

  * { box-sizing: border-box; }

  html { scroll-behavior: smooth; }

  body {
    margin: 0;
    background: var(--bg);
    color: var(--text);
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
    line-height: 1.65;
    font-size: 17px;
  }

  a { color: var(--accent); text-decoration: none; }
  a:hover { text-decoration: underline; }

  .topbar {
    background: var(--surface);
    border-bottom: 1px solid var(--border);
    position: sticky;
    top: 0;
    z-index: 10;
  }

  .topbar-inner {
    max-width: 920px;
    margin: 0 auto;
    padding: 0.75rem 1.25rem;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 1rem;
  }

  .brand { font-weight: 700; color: var(--text); }
  .brand span { color: var(--muted); font-weight: 500; }

  main {
    max-width: 920px;
    margin: 0 auto;
    padding: 2.5rem 1.25rem 4rem;
  }

  article {
    background: var(--surface);
    border: 1px solid var(--border);
    border-radius: 14px;
    padding: 2.5rem;
    box-shadow: 0 8px 30px rgba(15, 23, 42, 0.05);
  }

  h1, h2, h3, h4, h5, h6 {
    line-height: 1.25;
    margin: 2.2em 0 0.7em;
    scroll-margin-top: 4rem;
  }

  h1 { font-size: 2rem; margin-top: 0; }
  h2 {
    font-size: 1.45rem;
    padding-bottom: 0.35rem;
    border-bottom: 1px solid var(--border);
  }
  h3 { font-size: 1.15rem; }
  h4 { font-size: 1rem; }

  p { margin: 0.85em 0; }

  ul, ol { padding-left: 1.6rem; margin: 0.85em 0; }
  li { margin: 0.35em 0; }
  li > ul, li > ol { margin: 0.35em 0; }

  hr {
    border: 0;
    border-top: 1px solid var(--border);
    margin: 2.5rem 0;
  }

  blockquote {
    margin: 1.2rem 0;
    padding: 0.75rem 1.1rem;
    border-left: 4px solid var(--accent);
    background: var(--accent-soft);
    border-radius: 0 8px 8px 0;
    color: #1e3a8a;
  }

  blockquote p { margin: 0.3em 0; }

  code {
    font-family: "SFMono-Regular", ui-monospace, Consolas, "Liberation Mono", monospace;
    font-size: 0.88em;
    background: var(--inline-code);
    color: #312e81;
    padding: 0.12em 0.35em;
    border-radius: 5px;
  }

  pre {
    background: var(--code-bg);
    color: var(--code-text);
    padding: 1rem 1.2rem;
    border-radius: 10px;
    overflow-x: auto;
    line-height: 1.5;
    margin: 1.1rem 0;
  }

  pre code {
    background: transparent;
    color: inherit;
    padding: 0;
    font-size: 0.85rem;
    white-space: pre;
  }

  .table-wrapper { overflow-x: auto; margin: 1.2rem 0; }

  table {
    border-collapse: collapse;
    width: 100%;
    font-size: 0.92rem;
  }

  th, td {
    border: 1px solid var(--border);
    padding: 0.55rem 0.75rem;
    text-align: left;
    vertical-align: top;
  }

  thead th { background: #f1f5f9; }

  tbody tr:nth-child(even) { background: #fafbff; }

  img {
    max-width: 100%;
    height: auto;
    display: block;
    margin: 1.2rem auto;
    border: 1px solid var(--border);
    border-radius: 8px;
    background: #fff;
  }

  .pager {
    display: flex;
    justify-content: space-between;
    gap: 1rem;
    margin-top: 1.5rem;
    font-size: 0.92rem;
  }

  .pager a {
    background: var(--surface);
    border: 1px solid var(--border);
    border-radius: 999px;
    padding: 0.45rem 1rem;
    color: var(--text);
  }

  .pager a:hover { border-color: var(--accent); color: var(--accent); text-decoration: none; }

  footer {
    max-width: 920px;
    margin: 0 auto;
    padding: 0 1.25rem 3rem;
    color: var(--muted);
    font-size: 0.85rem;
    text-align: center;
  }

  .hero { text-align: center; margin-bottom: 2rem; }
  .hero h1 { font-size: 2.1rem; margin-bottom: 0.4rem; }
  .hero p { color: var(--muted); margin: 0; }

  .cards { display: grid; gap: 1rem; margin-top: 2rem; }

  .card {
    display: block;
    background: var(--surface);
    border: 1px solid var(--border);
    border-radius: 12px;
    padding: 1.25rem 1.5rem;
    color: var(--text);
    box-shadow: 0 4px 18px rgba(15, 23, 42, 0.04);
    transition: transform 0.15s ease, border-color 0.15s ease;
  }

  .card:hover {
    transform: translateY(-2px);
    border-color: var(--accent);
    text-decoration: none;
  }

  .card h2 {
    margin: 0 0 0.35rem;
    font-size: 1.15rem;
    border: 0;
    padding: 0;
  }

  .card p { margin: 0; color: var(--muted); font-size: 0.92rem; }

  .badge {
    display: inline-block;
    background: var(--accent-soft);
    color: #1e3a8a;
    border-radius: 999px;
    font-size: 0.75rem;
    font-weight: 700;
    padding: 0.1rem 0.6rem;
    margin-bottom: 0.45rem;
    letter-spacing: 0.02em;
  }

  @media (max-width: 640px) {
    body { font-size: 16px; }
    article { padding: 1.5rem 1.1rem; }
    h1 { font-size: 1.6rem; }
  }

  @media print {
    .topbar, .pager, footer { display: none; }
    body { background: #fff; }
    article { border: 0; box-shadow: none; padding: 0; }
  }
`;

const layout = ({ title, body, nav = "" }) => `<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>${escapeHtml(title)} · Documentación PC_26-27</title>
  <style>${styles}</style>
</head>
<body>
  <header class="topbar">
    <div class="topbar-inner">
      <a class="brand" href="index.html">PC_26-27 <span>· Documentación</span></a>
      <a href="https://github.com/SergioAntonLopezumu/PC_26-27">Repositorio ↗</a>
    </div>
  </header>
  <main>
${body}
    <nav class="pager">${nav}</nav>
  </main>
  <footer>Generado automáticamente a partir de los ficheros Markdown de <code>docs/</code>.</footer>
</body>
</html>
`;

/* ------------------------------------------------------------------ */
/* Generación                                                          */
/* ------------------------------------------------------------------ */

const plainText = (markdown) =>
  markdown
    .replace(/<!--[\s\S]*?-->/g, "")
    .replace(/!\[[^\]]*\]\([^)]*\)/g, "")
    .replace(/\[([^\]]+)\]\([^)]*\)/g, "$1")
    .replace(/`([^`]+)`/g, "$1")
    .replace(/[*_~]/g, "")
    .trim();

function extractTitle(markdown, fallback) {
  const heading = markdown.match(/^#\s+(.+)$/m);
  return heading ? plainText(heading[1]) : fallback;
}

function extractSummary(markdown) {
  const lines = markdown.replace(/<!--[\s\S]*?-->/g, "").replace(/\r\n?/g, "\n").split("\n");
  for (const line of lines) {
    const trimmed = line.trim();
    if (!trimmed || trimmed.startsWith("#") || trimmed.startsWith("|")) continue;
    if (/^(!\[|```|~~~|>|[-*+]\s|\d+[.)]\s|---)/.test(trimmed)) continue;
    const text = plainText(trimmed);
    if (text) return text.length > 180 ? `${text.slice(0, 177)}…` : text;
  }
  return "";
}

async function listFiles(dir, base = dir) {
  const entries = await readdir(dir, { withFileTypes: true });
  const files = [];
  for (const entry of entries) {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      if (full === siteDir) continue;
      files.push(...(await listFiles(full, base)));
    } else {
      files.push(full);
    }
  }
  return files;
}

async function main() {
  await rm(siteDir, { recursive: true, force: true });
  await mkdir(siteDir, { recursive: true });

  const allFiles = await listFiles(docsDir);
  const markdownFiles = allFiles
    .filter((file) => path.dirname(file) === docsDir && file.endsWith(".md"))
    .sort((a, b) => {
      const na = Number.parseInt(path.basename(a).match(/(\d+)/)?.[1] ?? "0", 10);
      const nb = Number.parseInt(path.basename(b).match(/(\d+)/)?.[1] ?? "0", 10);
      return na - nb || path.basename(a).localeCompare(path.basename(b));
    });

  if (markdownFiles.length === 0) {
    console.warn("No se han encontrado ficheros .md en docs/.");
    return;
  }

  const pages = [];

  for (const file of markdownFiles) {
    const markdown = await readFile(file, "utf8");
    const slug = path.basename(file, ".md");
    const title = extractTitle(markdown, slug);
    const summary = extractSummary(markdown);
    const number = title.match(/bolet[ií]n\s+(\d+)/i)?.[1] ?? slug.match(/(\d+)/)?.[1] ?? "";
    pages.push({ slug, title, summary, number });
  }

  for (let index = 0; index < pages.length; index += 1) {
    const page = pages[index];
    const file = path.join(docsDir, `${page.slug}.md`);
    const markdown = await readFile(file, "utf8");
    const html = markdownToHtml(markdown);

    const previous = pages[index - 1];
    const next = pages[index + 1];
    const nav = [
      previous ? `<a href="${previous.slug}.html">← ${escapeHtml(previous.title)}</a>` : "<span></span>",
      next ? `<a href="${next.slug}.html">${escapeHtml(next.title)} →</a>` : "<span></span>",
    ].join("");

    const body = `    <article>\n${html}\n    </article>`;
    await writeFile(path.join(siteDir, `${page.slug}.html`), layout({ title: page.title, body, nav }), "utf8");
    console.log(`  ✓ ${page.slug}.md → site/${page.slug}.html`);
  }

  const cards = pages
    .map((page) => {
      const badge = page.number ? `Boletín ${page.number}` : "Documento";
      return `      <a class="card" href="${page.slug}.html">
        <span class="badge">${escapeHtml(badge)}</span>
        <h2>${escapeHtml(page.title)}</h2>
        <p>${escapeHtml(page.summary)}</p>
      </a>`;
    })
    .join("\n");

  const indexBody = `    <section class="hero">
      <h1>Documentación del proyecto</h1>
      <p>Memorias de las sesiones de prácticas continuas de PC_26-27.</p>
    </section>
    <section class="cards">
${cards}
    </section>`;

  await writeFile(
    path.join(siteDir, "index.html"),
    layout({ title: "Índice", body: indexBody }),
    "utf8",
  );
  console.log("  ✓ site/index.html");

  // Copia de recursos (imágenes y documentos), conservando la estructura.
  let copied = 0;
  for (const file of allFiles) {
    const extension = path.extname(file).toLowerCase();
    if (!ASSET_EXTENSIONS.has(extension)) continue;
    const relative = path.relative(docsDir, file);
    const destination = path.join(siteDir, relative);
    await mkdir(path.dirname(destination), { recursive: true });
    await copyFile(file, destination);
    copied += 1;
  }
  console.log(`  ✓ ${copied} recursos copiados a site/`);
  console.log(`\nSitio generado en ${path.relative(process.cwd(), siteDir) || siteDir}`);
}

main().catch((error) => {
  console.error("Error generando el sitio:", error);
  process.exitCode = 1;
});

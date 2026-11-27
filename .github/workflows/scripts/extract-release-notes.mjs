import { execFileSync } from "node:child_process";
import { existsSync, mkdirSync, readFileSync, writeFileSync } from "node:fs";
import { join } from "node:path";

const version = process.env.BASE_VERSION?.trim();
if (!version) {
  console.error("BASE_VERSION is required.");
  process.exit(1);
}

const changelogPath = process.argv[2] ?? "CHANGELOG.md";
const outputDir = process.env.WHATSNEW_DIR ?? "distribution/whatsnew";
const limit = Number(process.env.WHATSNEW_LIMIT ?? "500");
const locales = ["en-US", "ja-JP"];

function escapeRegExp(value) {
  return value.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}

function cleanText(text) {
  return text
    .replace(/\[([^\]]+)\]\([^)]*\)/g, "$1")
    .replace(/\s+/g, " ")
    .trim();
}

function extractItems(markdown, targetVersion) {
  const lines = markdown.split(/\r?\n/);
  const headingPattern = new RegExp(
    `^## \\[?${escapeRegExp(targetVersion)}\\]?(\\(|\\s|$)`,
  );
  const startIndex = lines.findIndex((line) => headingPattern.test(line));
  if (startIndex === -1) {
    return [];
  }
  const items = [];
  for (let index = startIndex + 1; index < lines.length; index += 1) {
    const line = lines[index];
    if (/^## /.test(line)) {
      break;
    }
    const bullet = line.match(/^\s*[*+-]\s+(.*)$/);
    if (bullet) {
      items.push(cleanText(bullet[1]));
    } else if (items.length > 0 && line.trim() !== "" && !line.startsWith("#")) {
      items[items.length - 1] = cleanText(
        `${items[items.length - 1]} ${line.trim()}`,
      );
    }
  }
  return items.filter((item) => item !== "");
}

function fitToLimit(items, maxLength) {
  const selected = [];
  let length = 0;
  for (const item of items) {
    const line = `- ${item}`;
    const nextLength =
      selected.length === 0 ? line.length : length + 1 + line.length;
    if (nextLength > maxLength) {
      if (selected.length === 0) {
        selected.push(line.slice(0, maxLength));
      }
      break;
    }
    selected.push(line);
    length = nextLength;
  }
  return selected;
}

let items = [];
if (existsSync(changelogPath)) {
  items = extractItems(readFileSync(changelogPath, "utf8"), version);
}

let notes;
if (items.length > 0) {
  const selected = fitToLimit(items, limit);
  console.log(
    `Extracted ${items.length} items for version ${version}; writing ${selected.length} items within ${limit} characters.`,
  );
  notes = `${selected.join("\n")}\n`;
} else {
  const subject = execFileSync("git", ["log", "-1", "--pretty=%s"], {
    encoding: "utf8",
  }).trim();
  console.log(
    `No changelog items found for version ${version}; falling back to the latest commit subject.`,
  );
  notes = `${subject.slice(0, limit)}\n`;
}

mkdirSync(outputDir, { recursive: true });
for (const locale of locales) {
  writeFileSync(join(outputDir, `whatsnew-${locale}`), notes);
}

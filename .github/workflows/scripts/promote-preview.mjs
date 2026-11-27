import { readFile } from "node:fs/promises";

const token = process.env.GITHUB_TOKEN;
const repository = process.env.GITHUB_REPOSITORY;
const before = process.env.BEFORE_SHA;
const after = process.env.GITHUB_SHA;
const [owner, repo] = repository.split("/");

async function api(path, options = {}) {
  const response = await fetch(`https://api.github.com/repos/${owner}/${repo}${path}`, {
    ...options,
    headers: {
      Accept: "application/vnd.github+json",
      Authorization: `Bearer ${token}`,
      "X-GitHub-Api-Version": "2022-11-28",
      ...(options.body ? { "Content-Type": "application/json" } : {}),
    },
  });
  if (!response.ok) {
    throw new Error(`GitHub API ${path} failed: ${response.status} ${await response.text()}`);
  }
  return response.status === 204 ? null : response.json();
}

function sectionLines(body, heading) {
  const lines = (body ?? "").split("\n");
  const start = lines.findIndex((line) => line.trim() === heading);
  if (start === -1) return [];
  const collected = [];
  for (let i = start + 1; i < lines.length; i++) {
    if (lines[i].startsWith("## ")) break;
    collected.push(lines[i]);
  }
  return collected;
}

function summaryItems(body) {
  return sectionLines(body, "## Summary")
    .map((line) => line.trimEnd())
    .filter((line) => line.startsWith("- ") && !line.startsWith("- ["))
    .filter((line) => line.slice(2).trim().length > 0);
}

function checkItems(body) {
  return sectionLines(body, "## Check List")
    .map((line) => line.trimEnd())
    .filter((line) => /^- \[[ xX]\]\s+\S/.test(line));
}

function placeholder(body, heading, fallback) {
  const lines = sectionLines(body, heading)
    .map((line) => line.trim())
    .filter((line) => line.startsWith("-"));
  return lines[0] ?? fallback;
}

function includedNumbers(body) {
  const match = (body ?? "").match(/<!-- promoted-prs: ([0-9, ]*) -->/);
  if (!match) return new Set();
  return new Set(
    match[1]
      .split(",")
      .map((part) => Number(part.trim()))
      .filter((number) => Number.isInteger(number) && number > 0),
  );
}

function buildBody(summary, checks, included, summaryPlaceholder, checkPlaceholder) {
  const summaryText = summary.length > 0 ? summary.join("\n") : summaryPlaceholder;
  const checkText = checks.length > 0 ? checks.join("\n") : checkPlaceholder;
  const marker = included.length > 0 ? `\n\n<!-- promoted-prs: ${included.join(",")} -->` : "";
  return `## Summary\n\n${summaryText}\n\n## Check List\n\n${checkText}${marker}\n`;
}

async function main() {
  const comparison = await api("/compare/main...preview");
  if (comparison.ahead_by === 0) {
    console.log("preview is not ahead of main. Nothing to promote.");
    return;
  }

  let template = "";
  try {
    template = await readFile(".github/pull_request_template.md", "utf8");
  } catch {
    template = "";
  }
  const summaryPlaceholder = placeholder(template, "## Summary", "-");
  const checkPlaceholder = placeholder(template, "## Check List", "- [ ]");

  const openPulls = await api(`/pulls?state=open&base=main&head=${owner}:preview`);
  const promotion = openPulls[0] ?? null;

  const included = promotion ? includedNumbers(promotion.body) : new Set();
  const summary = promotion ? summaryItems(promotion.body) : [];
  const checks = promotion ? checkItems(promotion.body) : [];

  const merged = [];
  if (before && !/^0+$/.test(before) && before !== after) {
    const range = await api(`/compare/${before}...${after}`);
    const seen = new Set();
    for (const commit of range.commits ?? []) {
      const pulls = await api(`/commits/${commit.sha}/pulls`);
      for (const pull of pulls) {
        if (pull.merged_at && pull.base?.ref === "preview" && !seen.has(pull.number)) {
          seen.add(pull.number);
          merged.push(pull);
        }
      }
    }
  }

  for (const pull of merged) {
    if (included.has(pull.number)) continue;
    included.add(pull.number);
    const items = summaryItems(pull.body);
    if (items.length > 0) {
      for (const item of items) {
        if (!summary.includes(item)) summary.push(item);
      }
    } else {
      const fallback = `- ${pull.title} (#${pull.number})`;
      if (!summary.includes(fallback)) summary.push(fallback);
    }
    for (const item of checkItems(pull.body)) {
      if (!checks.includes(item)) checks.push(item);
    }
  }

  const body = buildBody(summary, checks, [...included], summaryPlaceholder, checkPlaceholder);

  if (promotion) {
    if (promotion.body !== body) {
      await api(`/pulls/${promotion.number}`, {
        method: "PATCH",
        body: JSON.stringify({ body }),
      });
      console.log(`Updated promotion pull request #${promotion.number}.`);
    } else {
      console.log(`Promotion pull request #${promotion.number} is already up to date.`);
    }
  } else {
    const created = await api("/pulls", {
      method: "POST",
      body: JSON.stringify({
        title: "Promote preview to main",
        head: "preview",
        base: "main",
        body,
      }),
    });
    console.log(`Created promotion pull request #${created.number}.`);
  }
}

await main();

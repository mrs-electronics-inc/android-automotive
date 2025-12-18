# Spec Guidelines

> This project uses the [Specture System](https://github.com/specture-system/specture), and this document outlines how it works. As the Specture System is improved and updated, this file will also be updated.

## Overview

Specs are design documents that describe planned changes to the system. They serve as a blueprint for implementation and a historical record of design decisions. They are continually improved during the design and implementation of a change, but left static after the change is complete.

This system is inspired by the [PEP](https://peps.python.org/) (Python Enhancement Proposal) and [BIP](https://github.com/bitcoin/bips) (Bitcoin Improvement Proposal) systems, adapting their formal proposal processes for general software development.

## Spec File Structure

Each spec file should be a markdown document with a numeric prefix in the `specs/` directory, for example `specs/000-mvp.md`.

### Frontmatter

Each spec should begin with YAML frontmatter containing metadata:

```markdown
---
status: draft
author: Your Name
created: 2025-12-18
---

# Spec Title
```

**Status values:**

- `draft` - Spec is in the process of being written and improved upon
- `approved` - Spec has been approved and is awaiting implementation
- `in-progress` - Implementation is underway
- `completed` - All tasks have been completed
- `rejected` - Spec was reviewed but rejected

**Optional fields:**

- `author` - Person who proposed or wrote the spec
- `created` - Date the spec was created (YYYY-MM-DD format)

### Title

The spec should start with a clear, descriptive H1 heading that summarizes what is being proposed.

```markdown
# Feature Name or Change Description
```

### Description

An overview of the proposed change. It might be a couple sentences for a small change or dozens of paragraphs for a large change.

A few things to consider including:

- What is being proposed
- Why it's needed
- What problem it solves
- High-level approach

Feel free to use paragraph form or bulleted list form, whichever better matches the requirements of clearly describing the proposal.

For large descriptions, please add separate sections with their own headers, for example: `## Ideas`, `## Goals`, `## Benefits`.

### Design Decisions

This section documents the design exploration process. For each major decision point, include the options considered along with the pros and cons of each.

Include as many decision points as needed. This creates a valuable historical record of why certain choices were made.

### Task List

A detailed breakdown of implementation tasks using markdown checklists. Split into logical sections if needed.

**Format:**

```markdown
## Task List

### Foundation

- [ ] Task 1
- [ ] Task 2
- [ ] Task 3

### Core Implementation

- [ ] Task 1
- [ ] Task 2

### Polish & Documentation

- [ ] Task 1
- [ ] Task 2
```

#### Task List Best Practices

- Make tasks specific and actionable
- Order tasks logically (dependencies first)
- Group related tasks into sections
- Include testing, documentation, and deployment tasks
- Keep individual tasks reasonably sized (completable in one session)

## File Naming

Use descriptive, kebab-case filenames with a numeric prefix:

- `000-mvp.md`
- `001-add-authentication-system.md`
- `013-refactor-database-layer.md`
- `314-redesign-api-endpoints.md`

## Workflow

### Draft

1. Create a new spec file with frontmatter status set to `draft`
2. Write the spec with all required sections (Title, Description, Design Decisions, Task List)
3. Keep the spec updated as you refine the proposal
4. Open pull/merge requests to share and refine the spec with the team
5. The spec can be merged multiple times while still in draft status as it evolves

#### Tips

- **Be clear, not clever**: Write for future readers who may not have context
- **Document alternatives**: Even rejected options are valuable to record
- **Update as you go**: Specs should evolve during implementation
- **Link to discussions**: Reference PR/MR comments, issues, or other specs
- **Focus on "why"**: The code shows "how", the spec should explain "why"

### Approved

It is up to the project maintainers to determine when they are ready to merge a spec with the status set to `approved`. The requirements for what defines an approved spec will vary by project.

1. Once the team agrees the spec is ready for implementation, update status to `approved` in a PR/MR
2. Merge the PR/MR with the updated status
3. Implementation can now begin

### In-Progress

1. Implementation begins once a spec is `approved`
2. Work through the task list, checking off items as completed
3. Keep the spec updated as implementation reveals new details
4. When all tasks are completed, move to `completed` status

### Completed

1. Mark the spec status as `completed` when all tasks in its task list are checked off
2. If there is still remaining work to be done for a change, be sure that the task list reflects this reality and keep the status as `in-progress`
3. Once a spec is marked as 'completed', it becomes a historical record and should not be retroactively updated
4. Any new requirements become a higher-numbered spec (see [Precedence System](#precedence-system))

### Rejected

For most rejected specs, the project maintainers will not merge the spec. There's no reason to merge a proposal you don't intend to implement.

However, some rejected specs may be useful to merge with the status set to `rejected`. These can act as a historical record of what was considered and rejected. The important thing in these cases is to clearly document **why** a proposal was rejected.

## Precedence System

The requirements listed in one spec may become outdated with time. Once a spec has status `completed`, it should be treated as a historical record and not retroactively updated. It is a bad idea to try to go back and update completed specs. This would be tedious and error-prone. Inevitably, something would be missed.

The exception is to fix overlooked mistakes-typos, errors in documentation, or factual inaccuracies should be corrected.

Instead, we rely on a simple precedence system. The numeric prefix at the beginning of each spec defines its precedence. The higher the number is, the higher the precedence. If two specs contradict each other on any particular point, the higher numbered spec takes precedence.

In general, the numbers should be incremented over time with each new spec added to the project.

Some tricky situations might arise where it becomes necessary to number specs non-incrementally, especially when a team is working on drafting multiple specs at once. A project's spec number assignment system should be optimized for the needs of that project. Overall, no matter what scheme you determine for assigning numbers to each new spec, stick to the rule that higher number means higher precedence.

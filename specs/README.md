# Spec Guidelines

> This project uses the [Specture System](https://github.com/specture-system/specture)

This document outlines the Specture System. As the Specture System is improved and updated, this file will also be updated.

## Overview

Specs are design documents that describe planned changes to the system. They serve as a blueprint for implementation and a historical record of design decisions. They are continually improved during the design and implementation of a change, but left static after the change is complete.

## Spec File Structure

Each spec file should be a markdown document with a numeric prefix in the `specs/` directory, for example `specs/000-mvp.md`.

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

For large descriptions, please add subsection headers, for example: `## Ideas`, `## Goals`, `## Benefits`.

### Design Decisions

This section documents the design exploration process. For each major decision point, include the options considered along with the pros and cons of each.

Include as many decision points as needed. This creates a valuable historical record of why certain choices were made.

### Task List

A detailed breakdown of implementation tasks using markdown checklists. Split into logical sections if needed.

**Format:**

```markdown
## Task List

### Phase 1: Foundation

- [ ] Task 1
- [ ] Task 2
- [ ] Task 3

### Phase 2: Core Implementation

- [ ] Task 1
- [ ] Task 2

### Phase 3: Polish & Documentation

- [ ] Task 1
- [ ] Task 2
```

**Task List Best Practices:**

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

1. **Create Spec**: Write the spec file with all required sections
2. **Submit PR/MR**: Open a pull/merge request adding the spec to `specs/`
3. **Discussion**: Team discusses and refines the spec in PR/MR comments
4. **Approval**: Once approved, merge the spec PR/MR
5. **Implementation**: Work through the task list, checking off items as completed
6. **Update**: Keep the spec updated as implementation reveals new details

### Tips

- **Be clear, not clever**: Write for future readers who may not have context
- **Document alternatives**: Even rejected options are valuable to record
- **Update as you go**: Specs should evolve during implementation
- **Link to discussions**: Reference PR/MR comments, issues, or other specs
- **Focus on "why"**: The code shows "how", the spec should explain "why"

## Completion

A spec is considered complete when all tasks in its task list are checked off. If there is still remaining work to be done for a change, be sure that the task list reflects this reality.

## Precedence System

The requirements listed in one spec may become outdated with time. Once a spec is complete (as defined in the Completion section), it should be treated as a historical record and not retroactively updated. It is a bad idea to try to go back and update completed specs. Inevitably, something will be missed.

The exception is to fix overlooked mistakes—typos, errors in documentation, or factual inaccuracies should be corrected.

Instead, we rely on a simple precedence system. The numeric prefix at the beginning of each spec defines its precedence. The higher the number is, the higher the precedence. If two specs contradict each other on any particular point, the higher numbered spec takes precedence.

In general, the numbers should be incremented over time with each new spec added to the project.

Some tricky situations might arise where it becomes necessary to number specs non-incrementally, especially when the team is working on drafting multiple specs at once. This is left to each project team to determine how to handle. Overall, no matter what scheme you determine for assigning numbers to each new spec, stick to the rule that higher number means higher precedence.

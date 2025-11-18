# Android Automotive

This is the repo for Android Automotive development at MRS Electronic, Inc.

More details will be added as we build out the project.

## New Project Checklist

Follow these steps in every new docs repository.

- [x] Use this template to create new repo - [link](https://github.com/new?template_name=basic-template&template_owner=mrs-electronics-inc)
- [x] Configure new repo with correct settings - [docs](https://hub.mrs-electronics.dev/project-management/github-set-up/)
- [x] Configure GitHub Pages
  - [x] "GitHub Actions" as source
  - [x] Add DNS config in AWS Route 53
  - [x] Add custom domain in GitHub
- [ ] Create a pull request
  - [x] Update `astro.config.mjs` with appropriate configuration
  - [ ] Update `src/content/docs/index.mdx` with appropriate information
  - [ ] Update `public/site.webmanifest` with appropriate information
  - [ ] Update `package.json` with project name
  - [ ] Add any initial content
  - [ ] Replace the `README.md` file with information actually relevant to the project
- [ ] Merge the pull request
- [ ] Verify that deployment is successful
- [ ] Enable "Enforce HTTPS" in the GitHub pages settings (it does not seem you can do it until you have a deployment)

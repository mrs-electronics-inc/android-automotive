// @ts-check
import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';

// https://astro.build/config
export default defineConfig({
	site: "https://android-automotive.mrs-electronics.dev",
	integrations: [
		starlight({
			plugins: [],
			title: 'Android Automotive',
			social: [
				{ icon: 'github', label: 'GitHub', href: 'https://github.com/mrs-electronics-inc/android-automotive' },
			],
			sidebar: [
				{
					label: "Getting Started",
					autogenerate: { directory: "getting-started" },
				},
			],
		}),
	],
});

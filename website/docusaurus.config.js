// @ts-check
// Note: type annotations allow type checking and IDEs autocompletion

const lightCodeTheme = require('prism-react-renderer/themes/github');
const darkCodeTheme = require('prism-react-renderer/themes/dracula');

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: 'WallPanel',
  tagline: 'Android application for web-based dashboards',
  url: 'https://wallpanel.xyz',
  baseUrl: '/',
  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',
  favicon: 'img/favicon.ico',
  organizationName: 'TheTimeWalker',
  projectName: 'wallpanel-android',

  presets: [
    [
      'classic',
      /** @type {import('@docusaurus/preset-classic').Options} */
      ({
        docs: {
          sidebarPath: require.resolve('./sidebars.js'),
          editUrl:
            'https://github.com/TheTimeWalker/wallpanel-android/tree/master/website/',
        },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      }),
    ],
  ],

  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      colorMode: {
        defaultMode: 'dark',
        disableSwitch: true,
      },
      navbar: {
        title: 'WallPanel',
        logo: {
          alt: 'WallPanel Logo',
          src: 'img/logo.png',
        },
        items: [
          {
            type: 'doc',
            docId: 'getting-started',
            position: 'left',
            label: 'Getting Started',
          },
          {
            to: 'privacy-policy',
            position: 'left',
            label: 'Privacy Policy',
          },
          {
            href: 'https://github.com/TheTimeWalker/wallpanel-android',
            label: 'GitHub',
            position: 'right',
          },
        ],
      },
      footer: {
        style: 'dark',
        copyright: `Copyright © ${new Date().getFullYear()} WallPanel Contributors. Built with Docusaurus.`,
      },
      prism: {
        theme: lightCodeTheme,
        darkTheme: darkCodeTheme,
      },
    }),
};

module.exports = config;

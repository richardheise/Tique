module.exports = ({ config }) => {
  if (process.env.FFORTUNA_MODE !== 'capture-poc') return config;
  return {
    ...config,
    name: 'Tique',
    slug: 'ffortuna-capture-poc',
    scheme: 'ffortuna-capture-poc',
    version: '1.1.2',
    icon: './assets/branding/icon.png',
    updates: { enabled: false },
    android: {
      ...config.android,
      package: 'org.ffortuna.capturepoc',
      versionCode: 18,
      allowBackup: false,
      icon: './assets/branding/icon.png',
      adaptiveIcon: {
        foregroundImage: './assets/branding/foreground.png',
        monochromeImage: './assets/branding/monochrome.png',
        backgroundColor: '#080B10',
      },
      blockedPermissions: [
        'android.permission.INTERNET', 'android.permission.SYSTEM_ALERT_WINDOW',
        'android.permission.READ_EXTERNAL_STORAGE', 'android.permission.WRITE_EXTERNAL_STORAGE',
      ],
    },
    plugins: [...config.plugins, './plugins/withCapturePoc'],
  };
};

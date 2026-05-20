module.exports = {
  extends: ['@commitlint/config-conventional'],
  rules: {
    'scope-enum': [2, 'always', [
      'backend', 'frontend', 'docker', 'ai', 'map',
      'search', 'geocoding', 'ci', 'deps', 'config'
    ]],
    'subject-max-length': [2, 'always', 100]
  }
};

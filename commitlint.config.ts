import type { UserConfig } from '@commitlint/types';

const config: UserConfig = {
  extends: ['@commitlint/config-conventional'],
  rules: {
    'type-enum': [2, 'always', ['feat', 'chore', 'fix', 'revert']],
    'scope-empty': [2, 'always'],
  },
};

export default config;

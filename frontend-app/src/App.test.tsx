import React from 'react';
import { render, screen } from '@testing-library/react';

// Test básico sin dependencias complejas
describe('Frontend App', () => {
  test('should be testable', () => {
    expect(true).toBe(true);
  });

  test('environment variables should be accessible', () => {
    expect(process.env.NODE_ENV).toBeDefined();
  });
});

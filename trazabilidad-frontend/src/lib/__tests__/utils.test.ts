import { describe, it, expect } from 'vitest';
import { cn } from '../utils';

describe('cn utility function', () => {
  it('should merge single class', () => {
    expect(cn('px-4')).toBe('px-4');
  });

  it('should merge multiple classes', () => {
    expect(cn('px-4', 'py-2')).toBe('px-4 py-2');
  });

  it('should handle conditional classes', () => {
    const isActive = true;
    expect(cn('base', isActive && 'active')).toBe('base active');
  });

  it('should filter out falsy values', () => {
    expect(cn('base', false, null, undefined, 'extra')).toBe('base extra');
  });

  it('should merge conflicting tailwind classes (last wins)', () => {
    expect(cn('px-4', 'px-8')).toBe('px-8');
  });

  it('should merge conflicting padding classes', () => {
    expect(cn('p-4', 'px-2')).toBe('p-4 px-2');
  });

  it('should handle array of classes', () => {
    expect(cn(['px-4', 'py-2'])).toBe('px-4 py-2');
  });

  it('should handle object notation', () => {
    expect(cn({ 'bg-red-500': true, 'text-white': false })).toBe('bg-red-500');
  });

  it('should handle empty inputs', () => {
    expect(cn()).toBe('');
    expect(cn('')).toBe('');
  });

  it('should handle complex tailwind merges', () => {
    expect(cn('bg-red-500 hover:bg-red-600', 'bg-blue-500')).toBe('hover:bg-red-600 bg-blue-500');
  });
});

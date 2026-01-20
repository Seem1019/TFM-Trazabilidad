import { describe, it, expect } from 'vitest';
import { render, screen } from '@/test/test-utils';
import { LoadingSpinner, PageLoader } from '../LoadingSpinner';

describe('LoadingSpinner', () => {
  it('renders without text by default', () => {
    render(<LoadingSpinner />);
    const spinner = document.querySelector('.animate-spin');
    expect(spinner).toBeInTheDocument();
  });

  it('renders with text when provided', () => {
    render(<LoadingSpinner text="Cargando datos..." />);
    expect(screen.getByText('Cargando datos...')).toBeInTheDocument();
  });

  it('applies small size classes', () => {
    render(<LoadingSpinner size="sm" />);
    const spinner = document.querySelector('.animate-spin');
    expect(spinner).toHaveClass('h-4', 'w-4');
  });

  it('applies medium size classes by default', () => {
    render(<LoadingSpinner />);
    const spinner = document.querySelector('.animate-spin');
    expect(spinner).toHaveClass('h-8', 'w-8');
  });

  it('applies large size classes', () => {
    render(<LoadingSpinner size="lg" />);
    const spinner = document.querySelector('.animate-spin');
    expect(spinner).toHaveClass('h-12', 'w-12');
  });

  it('applies custom className', () => {
    render(<LoadingSpinner className="custom-class" />);
    const container = document.querySelector('.flex');
    expect(container).toHaveClass('custom-class');
  });
});

describe('PageLoader', () => {
  it('renders with loading text', () => {
    render(<PageLoader />);
    expect(screen.getByText('Cargando...')).toBeInTheDocument();
  });

  it('renders large spinner', () => {
    render(<PageLoader />);
    const spinner = document.querySelector('.animate-spin');
    expect(spinner).toHaveClass('h-12', 'w-12');
  });

  it('has minimum height container', () => {
    render(<PageLoader />);
    const container = document.querySelector('.min-h-\\[400px\\]');
    expect(container).toBeInTheDocument();
  });
});

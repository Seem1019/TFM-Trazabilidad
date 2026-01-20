import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, waitFor, act } from '@testing-library/react';
import { useFetch } from '../useFetch';

describe('useFetch', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should start with loading state', () => {
    const fetchFn = vi.fn(() => new Promise<string>(() => {}));
    const { result } = renderHook(() => useFetch(fetchFn));

    expect(result.current.isLoading).toBe(true);
    expect(result.current.data).toBe(null);
    expect(result.current.error).toBe(null);
  });

  it('should return data on successful fetch', async () => {
    const mockData = { id: 1, name: 'Test' };
    const fetchFn = vi.fn(() => Promise.resolve(mockData));

    const { result } = renderHook(() => useFetch(fetchFn));

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.data).toEqual(mockData);
    expect(result.current.error).toBe(null);
    expect(fetchFn).toHaveBeenCalledTimes(1);
  });

  it('should return error on failed fetch', async () => {
    const errorMessage = 'Network error';
    const fetchFn = vi.fn(() => Promise.reject(new Error(errorMessage)));

    const { result } = renderHook(() => useFetch(fetchFn));

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.data).toBe(null);
    expect(result.current.error).toBe(errorMessage);
  });

  it('should handle non-Error rejection', async () => {
    const fetchFn = vi.fn(() => Promise.reject('string error'));

    const { result } = renderHook(() => useFetch(fetchFn));

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.error).toBe('Error al cargar datos');
  });

  it('should refetch data when refetch is called', async () => {
    let callCount = 0;
    const fetchFn = vi.fn(() => Promise.resolve({ count: ++callCount }));

    const { result } = renderHook(() => useFetch(fetchFn));

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.data).toEqual({ count: 1 });

    await act(async () => {
      await result.current.refetch();
    });

    expect(result.current.data).toEqual({ count: 2 });
    expect(fetchFn).toHaveBeenCalledTimes(2);
  });

  it('should allow manual data update via setData', async () => {
    const fetchFn = vi.fn(() => Promise.resolve({ value: 'initial' }));

    const { result } = renderHook(() => useFetch(fetchFn));

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    act(() => {
      result.current.setData({ value: 'updated' });
    });

    expect(result.current.data).toEqual({ value: 'updated' });
  });

  it('should allow setData with function updater', async () => {
    const fetchFn = vi.fn(() => Promise.resolve({ count: 5 }));

    const { result } = renderHook(() => useFetch<{ count: number }>(fetchFn));

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    act(() => {
      result.current.setData((prev) => prev ? { count: prev.count + 1 } : { count: 0 });
    });

    expect(result.current.data).toEqual({ count: 6 });
  });

  it('should refetch when dependencies change', async () => {
    const fetchFn = vi.fn((id: number) => Promise.resolve({ id }));

    const { result, rerender } = renderHook(
      ({ id }) => useFetch(() => fetchFn(id), [id]),
      { initialProps: { id: 1 } }
    );

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.data).toEqual({ id: 1 });

    rerender({ id: 2 });

    await waitFor(() => {
      expect(result.current.data).toEqual({ id: 2 });
    });

    expect(fetchFn).toHaveBeenCalledTimes(2);
  });
});

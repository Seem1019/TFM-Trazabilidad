import { useState, useEffect, useCallback } from 'react';

interface UseFetchState<T> {
  data: T | null;
  isLoading: boolean;
  error: string | null;
}

interface UseFetchReturn<T> extends UseFetchState<T> {
  refetch: () => Promise<void>;
  setData: React.Dispatch<React.SetStateAction<T | null>>;
}

export function useFetch<T>(
  fetchFn: () => Promise<T>,
  dependencies: unknown[] = []
): UseFetchReturn<T> {
  const [state, setState] = useState<UseFetchState<T>>({
    data: null,
    isLoading: true,
    error: null,
  });

  const fetchData = useCallback(async () => {
    setState((prev) => ({ ...prev, isLoading: true, error: null }));
    try {
      const result = await fetchFn();
      setState({ data: result, isLoading: false, error: null });
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Error al cargar datos';
      setState({ data: null, isLoading: false, error: message });
    }
  }, [fetchFn]);

  useEffect(() => {
    fetchData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, dependencies);

  const setData = useCallback((newData: React.SetStateAction<T | null>) => {
    setState((prev) => ({
      ...prev,
      data: typeof newData === 'function' ? (newData as (prev: T | null) => T | null)(prev.data) : newData,
    }));
  }, []);

  return {
    ...state,
    refetch: fetchData,
    setData,
  };
}

export default useFetch;

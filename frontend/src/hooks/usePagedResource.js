import { useCallback, useEffect, useState } from 'react';

const EMPTY_PAGE = {
  content: [],
  page: 0,
  size: 20,
  totalElements: 0,
  totalPages: 0,
  hasNext: false,
  hasPrevious: false,
};

export function usePagedResource(fetcher, deps = []) {
  const [data, setData] = useState(EMPTY_PAGE);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [reloadToken, setReloadToken] = useState(0);

  const reload = useCallback(() => setReloadToken((token) => token + 1), []);

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError(null);

    fetcher({ page, size: 20 })
      .then((result) => {
        if (active) setData(result);
      })
      .catch((err) => {
        if (active) setError(err.message || 'Error al consultar el backend');
      })
      .finally(() => {
        if (active) setLoading(false);
      });

    return () => {
      active = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, reloadToken, ...deps]);

  return { data, page, setPage, loading, error, reload };
}

export default function Pagination({ page, totalPages, hasNext, hasPrevious, onChange }) {
  if (totalPages <= 1) return null;

  return (
    <div className="pagination">
      <button disabled={!hasPrevious} onClick={() => onChange(page - 1)}>
        Anterior
      </button>
      <span>
        Página {page + 1} de {totalPages}
      </span>
      <button disabled={!hasNext} onClick={() => onChange(page + 1)}>
        Siguiente
      </button>
    </div>
  );
}

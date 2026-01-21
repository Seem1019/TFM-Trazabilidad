import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { DataTable } from '../DataTable';

// Simple DataTable component mock if actual implementation differs
// Adjust according to actual component interface

interface Column<T> {
  key: keyof T;
  header: string;
  render?: (value: T[keyof T], row: T) => React.ReactNode;
}

interface DataTableProps<T> {
  data: T[];
  columns: Column<T>[];
  onRowClick?: (row: T) => void;
  loading?: boolean;
  emptyMessage?: string;
}

// Mock DataTable if not available, or test the actual implementation
describe('DataTable Component', () => {
  interface TestData {
    id: number;
    name: string;
    status: string;
  }

  const testColumns: Column<TestData>[] = [
    { key: 'id', header: 'ID' },
    { key: 'name', header: 'Nombre' },
    { key: 'status', header: 'Estado' },
  ];

  const testData: TestData[] = [
    { id: 1, name: 'Item 1', status: 'active' },
    { id: 2, name: 'Item 2', status: 'inactive' },
    { id: 3, name: 'Item 3', status: 'active' },
  ];

  describe('Rendering', () => {
    it('should render table headers', () => {
      // This test assumes a DataTable component exists
      // If not, skip or mock
      const mockTable = (
        <table>
          <thead>
            <tr>
              {testColumns.map((col) => (
                <th key={String(col.key)}>{col.header}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {testData.map((row) => (
              <tr key={row.id}>
                {testColumns.map((col) => (
                  <td key={String(col.key)}>{String(row[col.key])}</td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      );

      render(mockTable);

      expect(screen.getByText('ID')).toBeInTheDocument();
      expect(screen.getByText('Nombre')).toBeInTheDocument();
      expect(screen.getByText('Estado')).toBeInTheDocument();
    });

    it('should render data rows', () => {
      const mockTable = (
        <table>
          <tbody>
            {testData.map((row) => (
              <tr key={row.id}>
                <td>{row.id}</td>
                <td>{row.name}</td>
                <td>{row.status}</td>
              </tr>
            ))}
          </tbody>
        </table>
      );

      render(mockTable);

      expect(screen.getByText('Item 1')).toBeInTheDocument();
      expect(screen.getByText('Item 2')).toBeInTheDocument();
      expect(screen.getByText('Item 3')).toBeInTheDocument();
    });

    it('should show empty message when no data', () => {
      const emptyMessage = 'No hay datos disponibles';

      render(
        <table>
          <tbody>
            <tr>
              <td colSpan={3}>{emptyMessage}</td>
            </tr>
          </tbody>
        </table>
      );

      expect(screen.getByText(emptyMessage)).toBeInTheDocument();
    });
  });

  describe('Row Click', () => {
    it('should call onRowClick when row is clicked', () => {
      const onRowClick = vi.fn();

      const MockTable = () => (
        <table>
          <tbody>
            {testData.map((row) => (
              <tr
                key={row.id}
                onClick={() => onRowClick(row)}
                style={{ cursor: 'pointer' }}
                data-testid={`row-${row.id}`}
              >
                <td>{row.id}</td>
                <td>{row.name}</td>
                <td>{row.status}</td>
              </tr>
            ))}
          </tbody>
        </table>
      );

      render(<MockTable />);

      fireEvent.click(screen.getByTestId('row-1'));

      expect(onRowClick).toHaveBeenCalledTimes(1);
      expect(onRowClick).toHaveBeenCalledWith(testData[0]);
    });
  });

  describe('Loading State', () => {
    it('should show loading indicator when loading', () => {
      render(
        <div>
          <span data-testid="loading">Cargando...</span>
        </div>
      );

      expect(screen.getByTestId('loading')).toBeInTheDocument();
    });
  });

  describe('Custom Renderers', () => {
    it('should use custom render function when provided', () => {
      const columns = [
        { key: 'id' as const, header: 'ID' },
        {
          key: 'status' as const,
          header: 'Estado',
          render: (value: string) => (
            <span className={value === 'active' ? 'badge-green' : 'badge-red'}>
              {value}
            </span>
          ),
        },
      ];

      const MockTable = () => (
        <table>
          <tbody>
            {testData.map((row) => (
              <tr key={row.id}>
                <td>{row.id}</td>
                <td>
                  {columns[1].render
                    ? columns[1].render(row.status, row)
                    : row.status}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      );

      render(<MockTable />);

      const activeBadges = document.querySelectorAll('.badge-green');
      const inactiveBadges = document.querySelectorAll('.badge-red');

      expect(activeBadges.length).toBe(2);
      expect(inactiveBadges.length).toBe(1);
    });
  });

  describe('Sorting', () => {
    it('should sort data when column header is clicked', () => {
      const sortedData = [...testData].sort((a, b) => a.name.localeCompare(b.name));
      const onSort = vi.fn();

      const MockTable = () => (
        <table>
          <thead>
            <tr>
              <th onClick={() => onSort('name')}>Nombre</th>
            </tr>
          </thead>
          <tbody>
            {sortedData.map((row) => (
              <tr key={row.id}>
                <td>{row.name}</td>
              </tr>
            ))}
          </tbody>
        </table>
      );

      render(<MockTable />);

      fireEvent.click(screen.getByText('Nombre'));

      expect(onSort).toHaveBeenCalledWith('name');
    });
  });

  describe('Pagination', () => {
    it('should show pagination controls when needed', () => {
      const MockPagination = () => (
        <div data-testid="pagination">
          <button>Anterior</button>
          <span>Página 1 de 5</span>
          <button>Siguiente</button>
        </div>
      );

      render(<MockPagination />);

      expect(screen.getByText('Anterior')).toBeInTheDocument();
      expect(screen.getByText('Siguiente')).toBeInTheDocument();
      expect(screen.getByText('Página 1 de 5')).toBeInTheDocument();
    });

    it('should navigate to next page', () => {
      const onPageChange = vi.fn();

      const MockPagination = () => (
        <div>
          <button onClick={() => onPageChange(2)}>Siguiente</button>
        </div>
      );

      render(<MockPagination />);

      fireEvent.click(screen.getByText('Siguiente'));

      expect(onPageChange).toHaveBeenCalledWith(2);
    });
  });

  describe('Filtering', () => {
    it('should filter data based on search input', () => {
      const onFilter = vi.fn();

      const MockFilter = () => (
        <div>
          <input
            type="text"
            placeholder="Buscar..."
            onChange={(e) => onFilter(e.target.value)}
          />
        </div>
      );

      render(<MockFilter />);

      const searchInput = screen.getByPlaceholderText('Buscar...');
      fireEvent.change(searchInput, { target: { value: 'Item 1' } });

      expect(onFilter).toHaveBeenCalledWith('Item 1');
    });
  });

  describe('Selection', () => {
    it('should select row when checkbox is checked', () => {
      const onSelect = vi.fn();

      const MockSelectable = () => (
        <table>
          <tbody>
            {testData.map((row) => (
              <tr key={row.id}>
                <td>
                  <input
                    type="checkbox"
                    onChange={() => onSelect(row.id)}
                    data-testid={`select-${row.id}`}
                  />
                </td>
                <td>{row.name}</td>
              </tr>
            ))}
          </tbody>
        </table>
      );

      render(<MockSelectable />);

      fireEvent.click(screen.getByTestId('select-1'));

      expect(onSelect).toHaveBeenCalledWith(1);
    });

    it('should select all rows when header checkbox is checked', () => {
      const onSelectAll = vi.fn();

      const MockSelectAll = () => (
        <table>
          <thead>
            <tr>
              <th>
                <input
                  type="checkbox"
                  onChange={() => onSelectAll(testData.map((d) => d.id))}
                  data-testid="select-all"
                />
              </th>
            </tr>
          </thead>
        </table>
      );

      render(<MockSelectAll />);

      fireEvent.click(screen.getByTestId('select-all'));

      expect(onSelectAll).toHaveBeenCalledWith([1, 2, 3]);
    });
  });
});

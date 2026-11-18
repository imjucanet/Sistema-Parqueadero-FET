import { useState } from 'react';
import PropietariosPage from './pages/PropietariosPage';
import VehiculosPage from './pages/VehiculosPage';
import EspaciosPage from './pages/EspaciosPage';
import MovimientosPage from './pages/MovimientosPage';
import GraphQLPage from './pages/GraphQLPage';
import { BASE_URL } from './api/client';

const TABS = [
  { key: 'propietarios', label: 'Propietarios', Component: PropietariosPage },
  { key: 'vehiculos', label: 'Vehículos', Component: VehiculosPage },
  { key: 'espacios', label: 'Espacios', Component: EspaciosPage },
  { key: 'movimientos', label: 'Movimientos', Component: MovimientosPage },
  { key: 'graphql', label: 'GraphQL', Component: GraphQLPage },
];

function App() {
  const [activeTab, setActiveTab] = useState(TABS[0].key);
  const ActiveComponent = TABS.find((tab) => tab.key === activeTab).Component;

  return (
    <div className="app">
      <header className="app-header">
        <h1>Parqueadero FET</h1>
        <p className="api-url">API: {BASE_URL}</p>
      </header>

      <nav className="tabs">
        {TABS.map((tab) => (
          <button
            key={tab.key}
            className={tab.key === activeTab ? 'active' : ''}
            onClick={() => setActiveTab(tab.key)}
          >
            {tab.label}
          </button>
        ))}
      </nav>

      <main>
        <ActiveComponent />
      </main>
    </div>
  );
}

export default App;

import { useEffect, useState } from 'react'
import './App.css'

import {
  getPortfolios,
  getPortfolioSummary,
  getPortfolioHoldings,
} from './api/aurumApi'

function App() {
  const [portfolios, setPortfolios] = useState([])
  const [selectedPortfolioId, setSelectedPortfolioId] = useState(null)
  const [summary, setSummary] = useState(null)
  const [holdings, setHoldings] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    async function loadPortfolios() {
      try {
        const data = await getPortfolios()

        setPortfolios(data)

        if (data.length > 0) {
          setSelectedPortfolioId(data[0].id)
        }
      } catch (err) {
        setError(err.message)
      } finally {
        setLoading(false)
      }
    }

    loadPortfolios()
  }, [])

  useEffect(() => {
    if (!selectedPortfolioId) {
      return
    }

    async function loadPortfolioData() {
      try {
        setLoading(true)
        setError(null)

        const [summaryData, holdingsData] = await Promise.all([
          getPortfolioSummary(selectedPortfolioId),
          getPortfolioHoldings(selectedPortfolioId),
        ])

        setSummary(summaryData)
        setHoldings(holdingsData)
      } catch (err) {
        setError(err.message)
      } finally {
        setLoading(false)
      }
    }

    loadPortfolioData()
  }, [selectedPortfolioId])

  function formatCurrency(value) {
    return new Intl.NumberFormat('en-GB', {
      style: 'currency',
      currency: 'GBP',
    }).format(value ?? 0)
  }

  function formatPercentage(value) {
    return `${Number(value ?? 0).toFixed(2)}%`
  }

  if (loading && portfolios.length === 0) {
    return <div className="status-screen">Loading Aurum...</div>
  }

  if (error && portfolios.length === 0) {
    return <div className="status-screen error">{error}</div>
  }

  return (
    <div className="app">
      <header className="header">
        <div>
          <h1>Aurum</h1>
          <p>Investment Portfolio Tracker</p>
        </div>
      </header>

      <main className="dashboard">
        <section className="dashboard-header">
          <div>
            <p className="eyebrow">PORTFOLIO</p>
            <h2>{summary?.portfolioName ?? 'Portfolio Overview'}</h2>

            {portfolios.length > 0 && (
              <select
                className="portfolio-select"
                value={selectedPortfolioId ?? ''}
                onChange={(event) =>
                  setSelectedPortfolioId(Number(event.target.value))
                }
              >
                {portfolios.map((portfolio) => (
                  <option
                    key={portfolio.id}
                    value={portfolio.id}
                  >
                    {portfolio.name}
                  </option>
                ))}
              </select>
            )}
          </div>

          <button className="primary-button">
            Add Transaction
          </button>
        </section>

        {error && (
          <div className="error-banner">
            {error}
          </div>
        )}

        <section className="summary-grid">
          <div className="summary-card">
            <span>Portfolio Value</span>
            <strong>
              {formatCurrency(summary?.totalMarketValue)}
            </strong>
          </div>

          <div className="summary-card">
            <span>Total P&amp;L</span>
            <strong>
              {formatCurrency(summary?.totalProfitLoss)}
            </strong>
          </div>

          <div className="summary-card">
            <span>Return</span>
            <strong>
              {formatPercentage(summary?.returnPercentage)}
            </strong>
          </div>

          <div className="summary-card">
            <span>Holdings</span>
            <strong>{holdings.length}</strong>
          </div>
        </section>

        <section className="panel">
          <div className="panel-header">
            <h3>Holdings</h3>
          </div>

          {holdings.length === 0 ? (
            <div className="empty-state">
              No holdings to display.
            </div>
          ) : (
            <div className="table-wrapper">
              <table className="holdings-table">
                <thead>
                  <tr>
                    <th>Asset</th>
                    <th>Quantity</th>
                    <th>Avg. Cost</th>
                    <th>Current Price</th>
                    <th>Market Value</th>
                    <th>Unrealised P&amp;L</th>
                  </tr>
                </thead>

                <tbody>
                  {holdings.map((holding) => (
                    <tr key={holding.assetId}>
                      <td>
                        <strong>{holding.symbol}</strong>
                        <span className="asset-name">
                          {holding.name}
                        </span>
                      </td>

                      <td>{Number(holding.quantity)}</td>

                      <td>
                        {formatCurrency(holding.averageCost)}
                      </td>

                      <td>
                        {formatCurrency(holding.currentPrice)}
                      </td>

                      <td>
                        {formatCurrency(holding.marketValue)}
                      </td>

                      <td>
                        {formatCurrency(
                          holding.unrealisedProfitLoss
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </main>
    </div>
  )
}

export default App
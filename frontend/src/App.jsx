import { useEffect, useState } from 'react'
import './App.css'

import {
  createTransaction,
  getAssets,
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
  const [assets, setAssets] = useState([])
  const [showTransactionForm, setShowTransactionForm] = useState(false)

  const [transactionForm, setTransactionForm] = useState({
    assetId: '',
    type: 'BUY',
    quantity: '',
    price: '',
    transactionDate: new Date().toISOString().split('T')[0],
  })

  useEffect(() => {
    async function loadInitialData() {
      try {
        const [portfolioData, assetData] = await Promise.all([
          getPortfolios(),
          getAssets(),
        ])

        setPortfolios(portfolioData)
        setAssets(assetData)

        if (portfolioData.length > 0) {
          setSelectedPortfolioId(portfolioData[0].id)
        }
      } catch (err) {
        setError(err.message)
      } finally {
        setLoading(false)
      }
    }

    loadInitialData()
  }, [])

  useEffect(() => {
    if (!selectedPortfolioId) {
      return
    }

    async function loadPortfolioData() {
      try {
        setLoading(true)
        setError(null)

        await refreshPortfolioData(selectedPortfolioId)
      } catch (err) {
        setError(err.message)
      } finally {
        setLoading(false)
      }
    }

    loadPortfolioData()
  }, [selectedPortfolioId])

  async function refreshPortfolioData(portfolioId) {
    const [summaryData, holdingsData] = await Promise.all([
      getPortfolioSummary(portfolioId),
      getPortfolioHoldings(portfolioId),
    ])

    setSummary(summaryData)
    setHoldings(holdingsData)
  }

  function formatCurrency(value) {
    return new Intl.NumberFormat('en-GB', {
      style: 'currency',
      currency: 'GBP',
    }).format(value ?? 0)
  }

  function formatPercentage(value) {
    return `${Number(value ?? 0).toFixed(2)}%`
  }

  function handleTransactionChange(event) {
    const { name, value } = event.target

    setTransactionForm((current) => ({
      ...current,
      [name]: value,
    }))
  }

  async function handleTransactionSubmit(event) {
    event.preventDefault()

    try {
      setError(null)

      await createTransaction(selectedPortfolioId, {
        assetId: Number(transactionForm.assetId),
        type: transactionForm.type,
        quantity: Number(transactionForm.quantity),
        price: Number(transactionForm.price),
        transactionDate: transactionForm.transactionDate,
      })

      await refreshPortfolioData(selectedPortfolioId)

      setTransactionForm({
        assetId: '',
        type: 'BUY',
        quantity: '',
        price: '',
        transactionDate: new Date().toISOString().split('T')[0],
      })

      setShowTransactionForm(false)
    } catch (err) {
      setError(err.message)
    }
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

          <button
            className="primary-button"
            onClick={() => setShowTransactionForm(true)}
          >
            Add Transaction
          </button>

        </section>

        {error && (
          <div className="error-banner">
            {error}
          </div>
        )}

        {showTransactionForm && (
          <section className="transaction-panel">
            <div className="panel-header transaction-header">
              <h3>Add Transaction</h3>

              <button
                className="secondary-button"
                onClick={() => setShowTransactionForm(false)}
              >
                Cancel
              </button>
            </div>

            <form
              className="transaction-form"
              onSubmit={handleTransactionSubmit}
            >
              <label>
                Asset
                <select
                  name="assetId"
                  value={transactionForm.assetId}
                  onChange={handleTransactionChange}
                  required
                >
                  <option value="">Select asset</option>

                  {assets.map((asset) => (
                    <option key={asset.id} value={asset.id}>
                      {asset.symbol} — {asset.name}
                    </option>
                  ))}
                </select>
              </label>

              <label>
                Type
                <select
                  name="type"
                  value={transactionForm.type}
                  onChange={handleTransactionChange}
                >
                  <option value="BUY">Buy</option>
                  <option value="SELL">Sell</option>
                </select>
              </label>

              <label>
                Quantity
                <input
                  name="quantity"
                  type="number"
                  min="0"
                  step="0.00000001"
                  value={transactionForm.quantity}
                  onChange={handleTransactionChange}
                  required
                />
              </label>

              <label>
                Price
                <input
                  name="price"
                  type="number"
                  min="0"
                  step="0.0001"
                  value={transactionForm.price}
                  onChange={handleTransactionChange}
                  required
                />
              </label>

              <label>
                Date
                <input
                  name="transactionDate"
                  type="date"
                  value={transactionForm.transactionDate}
                  onChange={handleTransactionChange}
                  required
                />
              </label>

              <button
                className="primary-button"
                type="submit"
              >
                Save Transaction
              </button>
            </form>
          </section>
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
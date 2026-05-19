import React from "react";
import ReactDOM from "react-dom/client";
import {
  Activity,
  AlertTriangle,
  CheckCircle2,
  Clock3,
  Database,
  ExternalLink,
  RefreshCw,
  Send,
  ShieldAlert,
  TimerReset,
  Workflow
} from "lucide-react";
import "./styles.css";

type FailureMode = "NONE" | "TEMPORARY" | "PERMANENT";
type RecoveryStatus =
  | "RECEIVED"
  | "PROCESSING"
  | "SUCCEEDED"
  | "RETRY_PENDING"
  | "RETRYING"
  | "DEAD_LETTER"
  | "PERMANENT_FAILURE";

type RecoveryRecord = {
  idempotencyKey: string;
  operationType: string;
  status: RecoveryStatus;
  failureType: FailureMode;
  attemptCount: number;
  nextRetryAt: string | null;
  lastError: string | null;
  createdAt: string;
  updatedAt: string;
};

type FormState = {
  idempotencyKey: string;
  operationType: string;
  payload: string;
  failureMode: FailureMode;
  failuresBeforeSuccess: number;
};

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

const initialForm: FormState = {
  idempotencyKey: `order-${Date.now()}`,
  operationType: "PAYMENT",
  payload: "{\"amount\": 900, \"currency\": \"INR\"}",
  failureMode: "TEMPORARY",
  failuresBeforeSuccess: 2
};

function App() {
  const [records, setRecords] = React.useState<RecoveryRecord[]>([]);
  const [form, setForm] = React.useState<FormState>(initialForm);
  const [loading, setLoading] = React.useState(false);
  const [submitting, setSubmitting] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);
  const [lastUpdated, setLastUpdated] = React.useState<string | null>(null);

  const fetchRecords = React.useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await fetch(`${API_BASE_URL}/api/recovery`);
      if (!response.ok) {
        throw new Error(`API returned ${response.status}`);
      }
      const data = (await response.json()) as RecoveryRecord[];
      setRecords(data);
      setLastUpdated(new Date().toLocaleTimeString());
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "Failed to load records");
    } finally {
      setLoading(false);
    }
  }, []);

  React.useEffect(() => {
    fetchRecords();
    const timer = window.setInterval(fetchRecords, 5000);
    return () => window.clearInterval(timer);
  }, [fetchRecords]);

  async function submitRequest(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      const response = await fetch(`${API_BASE_URL}/api/recovery/execute`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(form)
      });
      if (!response.ok) {
        throw new Error(`Submit failed with ${response.status}`);
      }
      await fetchRecords();
      setForm((current) => ({ ...current, idempotencyKey: `order-${Date.now()}` }));
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "Failed to submit request");
    } finally {
      setSubmitting(false);
    }
  }

  const stats = buildStats(records);

  return (
    <main className="app-shell">
      <section className="topbar">
        <div>
          <p className="eyebrow">Distributed Recovery Control Plane</p>
          <h1>Automated Failure Recovery</h1>
        </div>
        <button className="icon-button" type="button" onClick={fetchRecords} disabled={loading} title="Refresh records">
          <RefreshCw size={18} />
          <span>{loading ? "Refreshing" : "Refresh"}</span>
        </button>
      </section>

      <section className="metrics-grid" aria-label="Recovery summary">
        <MetricCard icon={<Database size={20} />} label="Total Requests" value={stats.total} accent="neutral" />
        <MetricCard icon={<CheckCircle2 size={20} />} label="Succeeded" value={stats.succeeded} accent="success" />
        <MetricCard icon={<Clock3 size={20} />} label="Pending Retries" value={stats.pending} accent="pending" />
        <MetricCard icon={<TimerReset size={20} />} label="Retrying" value={stats.retrying} accent="info" />
        <MetricCard icon={<ShieldAlert size={20} />} label="Permanent Closed" value={stats.closed} accent="danger" />
        <MetricCard icon={<Activity size={20} />} label="Retry Success Rate" value={`${stats.successRate}%`} accent="success" />
      </section>

      <section className="content-grid">
        <form className="panel request-panel" onSubmit={submitRequest}>
          <div className="panel-header">
            <div>
              <p className="eyebrow">Create Scenario</p>
              <h2>Submit Recovery Request</h2>
            </div>
            <Workflow size={22} />
          </div>

          <label>
            Idempotency Key
            <input
              value={form.idempotencyKey}
              onChange={(event) => setForm({ ...form, idempotencyKey: event.target.value })}
              required
            />
          </label>

          <label>
            Operation
            <select
              value={form.operationType}
              onChange={(event) => setForm({ ...form, operationType: event.target.value })}
            >
              <option value="PAYMENT">PAYMENT</option>
              <option value="ORDER">ORDER</option>
              <option value="INVENTORY">INVENTORY</option>
            </select>
          </label>

          <label>
            Failure Mode
            <select
              value={form.failureMode}
              onChange={(event) => setForm({ ...form, failureMode: event.target.value as FailureMode })}
            >
              <option value="NONE">Success</option>
              <option value="TEMPORARY">Temporary Failure</option>
              <option value="PERMANENT">Permanent Failure</option>
            </select>
          </label>

          <label>
            Failures Before Success
            <input
              type="number"
              min="0"
              value={form.failuresBeforeSuccess}
              onChange={(event) => setForm({ ...form, failuresBeforeSuccess: Number(event.target.value) })}
            />
          </label>

          <label>
            Payload
            <textarea
              value={form.payload}
              onChange={(event) => setForm({ ...form, payload: event.target.value })}
              rows={5}
              required
            />
          </label>

          <button className="primary-button" type="submit" disabled={submitting}>
            <Send size={18} />
            <span>{submitting ? "Submitting" : "Submit Request"}</span>
          </button>
        </form>

        <section className="panel flow-panel">
          <div className="panel-header">
            <div>
              <p className="eyebrow">Runtime Flow</p>
              <h2>Recovery Pipeline</h2>
            </div>
            <Activity size={22} />
          </div>
          <div className="flow-lane">
            <FlowStep label="API accepts request" detail="MySQL idempotency record" />
            <FlowStep label="Kafka failure event" detail="api-failed-events" />
            <FlowStep label="Worker schedules retry" detail="Exponential backoff" />
            <FlowStep label="Redis lock acquired" detail="Duplicate retry guard" />
            <FlowStep label="Payment service call" detail="Circuit breaker protected" />
            <FlowStep label="Success or DLQ" detail="Final state persisted" />
          </div>

          <div className="links-panel">
            <a href={`${API_BASE_URL}/actuator/health`} target="_blank" rel="noreferrer">
              API Health <ExternalLink size={14} />
            </a>
            <a href={`${API_BASE_URL}/actuator/prometheus`} target="_blank" rel="noreferrer">
              API Metrics <ExternalLink size={14} />
            </a>
            <a href="http://localhost:8090" target="_blank" rel="noreferrer">
              Kafka UI <ExternalLink size={14} />
            </a>
          </div>
        </section>
      </section>

      <section className="panel table-panel">
        <div className="panel-header">
          <div>
            <p className="eyebrow">Live State</p>
            <h2>Recovery Requests</h2>
          </div>
          <p className="updated-text">{lastUpdated ? `Updated ${lastUpdated}` : "Waiting for data"}</p>
        </div>

        {error && (
          <div className="error-box">
            <AlertTriangle size={18} />
            <span>{error}</span>
          </div>
        )}

        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Idempotency Key</th>
                <th>Operation</th>
                <th>Status</th>
                <th>Failure</th>
                <th>Attempts</th>
                <th>Next Retry</th>
                <th>Last Error</th>
              </tr>
            </thead>
            <tbody>
              {records.length === 0 ? (
                <tr>
                  <td colSpan={7} className="empty-cell">No recovery requests yet.</td>
                </tr>
              ) : (
                records.map((record) => (
                  <tr key={record.idempotencyKey}>
                    <td>{record.idempotencyKey}</td>
                    <td>{record.operationType}</td>
                    <td><StatusBadge status={record.status} /></td>
                    <td>{record.failureType}</td>
                    <td>{record.attemptCount}</td>
                    <td>{formatDate(record.nextRetryAt)}</td>
                    <td className="error-text">{record.lastError ?? "-"}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </section>
    </main>
  );
}

function MetricCard({ icon, label, value, accent }: { icon: React.ReactNode; label: string; value: string | number; accent: string }) {
  return (
    <article className={`metric-card ${accent}`}>
      <div className="metric-icon">{icon}</div>
      <div>
        <p>{label}</p>
        <strong>{value}</strong>
      </div>
    </article>
  );
}

function FlowStep({ label, detail }: { label: string; detail: string }) {
  return (
    <div className="flow-step">
      <span />
      <div>
        <strong>{label}</strong>
        <p>{detail}</p>
      </div>
    </div>
  );
}

function StatusBadge({ status }: { status: RecoveryStatus }) {
  const className = status.toLowerCase().replace("_", "-");
  return <span className={`status-badge ${className}`}>{status}</span>;
}

function buildStats(records: RecoveryRecord[]) {
  const succeeded = records.filter((record) => record.status === "SUCCEEDED").length;
  const pending = records.filter((record) => record.status === "RETRY_PENDING").length;
  const retrying = records.filter((record) => record.status === "RETRYING").length;
  const closed = records.filter((record) => record.status === "DEAD_LETTER" || record.status === "PERMANENT_FAILURE").length;
  const completed = succeeded + closed;
  const successRate = completed === 0 ? 0 : Math.round((succeeded / completed) * 100);
  return { total: records.length, succeeded, pending, retrying, closed, successRate };
}

function formatDate(value: string | null) {
  if (!value) {
    return "-";
  }
  return new Date(value).toLocaleTimeString();
}

ReactDOM.createRoot(document.getElementById("root") as HTMLElement).render(<App />);


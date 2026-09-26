import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import "./Dashboard.css";
import "./LoanProducts.css";

const PRODUCTS = [
  {
    type: "Personal",
    icon: "fa-user-tie",
    amount: "₹1,000 - ₹100,000",
    rate: "Starting at 6.99% APR",
    terms: "1-7 years",
    features: ["No collateral required", "Fast approval", "Flexible use"],
    perfectFor: "Debt consolidation, medical expenses, vacations, or any personal need",
  },
  {
    type: "Auto",
    icon: "fa-car",
    amount: "Up to 100% of vehicle value",
    rate: "Starting at 4.25% APR",
    terms: "2-7 years",
    features: ["Competitive rates for new/used vehicles", "Quick approval"],
    perfectFor: "New car purchases, used vehicles, refinancing",
  },
  {
    type: "Home",
    icon: "fa-home",
    amount: "Up to ₹5,000,000",
    rate: "Fixed and adjustable options",
    terms: "10-30 years",
    features: ["Low down payment options", "First-time buyer programs"],
    perfectFor: "Home purchases, refinancing, home equity lines",
  },
  {
    type: "Education",
    icon: "fa-graduation-cap",
    amount: "Up to ₹200,000",
    rate: "Starting at 3.99% APR",
    terms: "Up to 15 years",
    features: ["Deferred payment options", "Cosigner release"],
    perfectFor: "Undergraduate, graduate, and professional degree programs",
  },
];

const BENEFITS = [
  { icon: "fa-percentage", title: "Competitive Rates", text: "Lowest interest rates in the market" },
  { icon: "fa-bolt", title: "Fast Approval", text: "Quick online approval process" },
  { icon: "fa-calendar-alt", title: "Flexible Terms", text: "Customizable repayment options" },
  { icon: "fa-user-tie", title: "Expert Advice", text: "Dedicated loan specialists" },
  { icon: "fa-eye-slash", title: "No Hidden Fees", text: "Transparent pricing" },
];

const TESTIMONIALS = [
  {
    img: "/Images/Gurunathsir.jpg",
    text: "At SysBank, we ensure fast processing and transparency for every auto loan. Our goal is to empower customers with quick approvals.",
    name: "Gurunath Kadam",
    role: "Auto Loan Consultant",
  },
  {
    img: "/Images/Kirtimam.jpg",
    text: "We customize education loan solutions to fit every family’s needs. Helping students succeed is the most rewarding part of my work.",
    name: "Kirti Khade",
    role: "Education Loan Advisor",
  },
  {
    img: "/Images/LoanConsultant-Darshana.png",
    text: "With strategic refinancing, we’ve helped hundreds save monthly on their home loans. It’s about making smart financial choices.",
    name: "Darshana Sawant",
    role: "Home Loan Specialist",
  },
];

export default function LoanProducts() {
  const { logout } = useAuth();

  return (
    <div className="dashboard-page loan-products-page">
      <nav className="navbar navbar-expand-lg navbar-dark mb-4">
        <div className="container">
          <Link className="navbar-brand" to="/dashboard">
            <i className="fas fa-university me-2"></i>SysBank
          </Link>
          <button className="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
            <span className="navbar-toggler-icon"></span>
          </button>
          <div className="collapse navbar-collapse" id="navbarNav">
            <ul className="navbar-nav ms-auto">
              <li className="nav-item"><Link className="nav-link" to="/dashboard"><i className="fas fa-home me-1"></i>Home</Link></li>
              <li className="nav-item"><Link className="nav-link" to="/profile"><i className="fas fa-user me-1"></i>Profile</Link></li>
              <li className="nav-item"><Link className="nav-link" to="/transactions"><i className="fas fa-exchange-alt me-1"></i>Transactions</Link></li>
              <li className="nav-item"><Link className="nav-link active" to="/loans"><i className="fas fa-hand-holding-usd me-1"></i>Loans</Link></li>
              <li className="nav-item"><Link className="nav-link" to="/tickets"><i className="fas fa-life-ring me-1"></i>Support</Link></li>
              <li className="nav-item">
                <a className="nav-link logout-btn" href="#" onClick={(e) => { e.preventDefault(); logout(); }}>
                  <i className="fas fa-sign-out-alt me-1"></i> Logout
                </a>
              </li>
            </ul>
          </div>
        </div>
      </nav>

      <div className="container">
        <div className="dashboard-header text-center mb-4 loan-products-hero">
          <h2 className="mb-2"><i className="fas fa-hand-holding-usd me-2"></i>Personalize Your Financing with SysBank</h2>
          <p className="mb-3">Every financial need is unique &mdash; choose from our range of flexible loan options designed to help you achieve your personal and professional goals.</p>
          <Link to="/loans" className="btn btn-primary-custom loan-products-cta">
            <i className="fas fa-file-signature"></i> Apply for a Loan
          </Link>
        </div>

        <h4 className="loan-products-section-title">Our Loan Products</h4>
        <div className="row g-4 mb-5">
          {PRODUCTS.map((p) => (
            <div className="col-md-6 col-lg-3" key={p.type}>
              <div className="card h-100 loan-product-card">
                <div className="card-body">
                  <i className={`fas ${p.icon} loan-product-icon`}></i>
                  <h5>{p.type} Loans</h5>
                  <ul className="loan-product-features">
                    <li><strong>Loan Amount:</strong> {p.amount}</li>
                    <li><strong>Interest Rate:</strong> {p.rate}</li>
                    <li><strong>Terms:</strong> {p.terms}</li>
                    {p.features.map((f) => <li key={f}>{f}</li>)}
                  </ul>
                  <p className="loan-product-perfect-for"><strong>Perfect for:</strong> {p.perfectFor}</p>
                  <Link to={`/loans?type=${encodeURIComponent(p.type)}`} className="btn btn-outline-primary btn-sm w-100">
                    Apply for a {p.type} Loan
                  </Link>
                </div>
              </div>
            </div>
          ))}
        </div>

        <h4 className="loan-products-section-title">Why Choose SysBank Loans?</h4>
        <div className="row g-4 mb-5">
          {BENEFITS.map((b) => (
            <div className="col-6 col-md-4 col-lg" key={b.title}>
              <div className="card h-100 text-center loan-benefit-card">
                <div className="card-body">
                  <i className={`fas ${b.icon} loan-benefit-icon`}></i>
                  <h6>{b.title}</h6>
                  <p className="mb-0 small text-muted">{b.text}</p>
                </div>
              </div>
            </div>
          ))}
        </div>

        <h4 className="loan-products-section-title">What Our Loan Experts Say</h4>
        <div className="row g-4 mb-5">
          {TESTIMONIALS.map((t) => (
            <div className="col-md-4" key={t.name}>
              <div className="card h-100 loan-testimonial-card">
                <div className="card-body">
                  <p className="fst-italic">&ldquo;{t.text}&rdquo;</p>
                  <div className="loan-testimonial-author">
                    <img src={t.img} alt={t.name} className="loan-testimonial-img" />
                    <span>{t.name}, <span className="text-muted">{t.role}</span></span>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>

        <div className="text-center mb-5">
          <Link to="/loans" className="btn btn-primary-custom loan-products-cta">
            <i className="fas fa-file-signature"></i> Apply for a Loan
          </Link>
        </div>
      </div>
    </div>
  );
}

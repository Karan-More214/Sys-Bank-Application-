import { useEffect, useRef } from "react";
import { Link } from "react-router-dom";
import "./Landing.css";

const API_BASE = import.meta.env.VITE_API_BASE_URL;

const TESTIMONIALS = [
  { img: "/Images/karan.jpg", quote: "“SysBank has completely transformed how I manage my finances…”", name: "Karan More", role: "Founder Of Sys Bank" },
  { img: "/Images/Azim.jpg", quote: "“The investment tools and advice from SysBank helped me grow my retirement savings…”", name: "Azeem Ansari", role: "Owner Of Sys Bank" },
  { img: "/Images/Aayush.jpg", quote: "“As someone who travels frequently, I love how easy it is to bank with SysBank…”", name: "Aayush Chalke", role: "International Consultant" },
  { img: "/Images/Santosh.jpg", quote: "“SysBank has completely transformed how I manage my finances — from intuitive interfaces to lightning-fast support, it’s simply the best.”", name: "Santosh Thakur", role: "Small Business Owner" },
  { img: "/Images/suraj.jpg", quote: "“Thanks to SysBank’s smart investment insights, I’ve been able to diversify my portfolio and plan my future with confidence.”", name: "Suraj Gupta", role: "Retired Government Officer" },
  { img: "/Images/yash.jpg", quote: "“Wherever I go in the world, SysBank travels with me. Their mobile banking experience is seamless and secure.”", name: "Yash Mirashi", role: "Frequent Business Traveler" },
];

export default function Landing() {
  const carouselRef = useRef(null);

  useEffect(() => {
    const onScroll = () => {
      const nav = document.querySelector(".landing-page .navbar");
      if (!nav) return;
      if (window.scrollY > 50) nav.classList.add("scrolled");
      else nav.classList.remove("scrolled");
    };
    window.addEventListener("scroll", onScroll);
    return () => window.removeEventListener("scroll", onScroll);
  }, []);

  useEffect(() => {
    let instance;
    (async () => {
      const { Carousel } = await import("bootstrap");
      if (carouselRef.current) {
        instance = new Carousel(carouselRef.current, { ride: "carousel" });
      }
    })();
    return () => instance?.dispose();
  }, []);

  return (
    <div className="landing-page">
      {/* Navbar */}
      <nav className="navbar navbar-expand-lg navbar-dark fixed-top">
        <div className="container">
          <a className="navbar-brand" href="#">
            <i className="fas fa-university"></i>SysBank
          </a>
          <button className="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
            <span className="navbar-toggler-icon"></span>
          </button>
          <div className="collapse navbar-collapse" id="navbarNav">
            <ul className="navbar-nav ms-auto">
              <li className="nav-item"><a className="nav-link active" href="#">Home</a></li>
              <li className="nav-item"><a className="nav-link" href="#about">About</a></li>
              <li className="nav-item"><a className="nav-link" href="#services">Services</a></li>
              <li className="nav-item"><a className="nav-link" href="#testimonials">Testimonials</a></li>
              <li className="nav-item"><a className="nav-link" href="#contact">Contact</a></li>
            </ul>
          </div>
        </div>
      </nav>

      {/* Hero Section */}
      <section className="hero-section">
        <div className="container">
          <div className="row align-items-center">
            <div className="col-lg-6 hero-content">
              <div className="security-badge animate__animated animate__fadeIn">
                <i className="fas fa-shield-alt"></i>
                <span>FDIC Secure &bull; 256-bit Encryption</span>
              </div>
              <h1 className="hero-title animate__animated animate__fadeInUp">Banking Reimagined for the Digital Age</h1>
              <p className="hero-subtitle animate__animated animate__fadeInUp animate__delay-1s">
                Experience seamless banking with our cutting-edge digital platform designed for your financial needs. No hidden fees, no paperwork, just smart banking.
              </p>

              <ul className="feature-list animate__animated animate__fadeInUp animate__delay-1s">
                <li><i className="fas fa-check"></i> 24/7 Account Access with Mobile App</li>
                <li><i className="fas fa-check"></i> Instant Money Transfers &amp; Payments</li>
                <li><i className="fas fa-check"></i> Advanced Security &amp; Fraud Protection</li>
                <li><i className="fas fa-check"></i> Personalized Financial Insights &amp; Tools</li>
              </ul>

              <div className="d-flex flex-wrap gap-3 animate__animated animate__fadeInUp animate__delay-2s">
                <a href={`${API_BASE}/main/create`} className="btn btn-primary btn-lg">
                  <i className="fas fa-user-plus me-2"></i>Open Account
                </a>
                <a href="#app-download" className="btn btn-outline-primary btn-lg">
                  <i className="fas fa-mobile-alt me-2"></i>Get Mobile App
                </a>
              </div>
            </div>
            <div className="col-lg-6 d-none d-lg-block animate__animated animate__fadeInRight">
              <div id="heroCarousel" className="carousel slide" ref={carouselRef}>
                <div className="carousel-inner rounded-4 shadow-lg" style={{ border: "12px solid white" }}>
                  <div className="carousel-item active">
                    <img src="/Images/Carosal5.jpg" className="d-block w-100" alt="Professional banking team" />
                  </div>
                  <div className="carousel-item">
                    <img src="/Images/carosal6.jpg" className="d-block w-100" alt="Mobile banking app" />
                  </div>
                  <div className="carousel-item">
                    <img src="/Images/Carosal7.jpg" className="d-block w-100" alt="Financial meeting" />
                  </div>
                </div>
                <button className="carousel-control-prev" type="button" data-bs-target="#heroCarousel" data-bs-slide="prev">
                  <span className="carousel-control-prev-icon" aria-hidden="true"></span>
                  <span className="visually-hidden">Previous</span>
                </button>
                <button className="carousel-control-next" type="button" data-bs-target="#heroCarousel" data-bs-slide="next">
                  <span className="carousel-control-next-icon" aria-hidden="true"></span>
                  <span className="visually-hidden">Next</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Account Choice Section */}
      <section className="section section-white" id="account-choice">
        <div className="container">
          <div className="text-center mb-5">
            <h2 className="section-title">Get Started with SysBank</h2>
            <p className="section-subtitle">Join millions of customers who trust us with their financial needs</p>
          </div>

          <div className="row g-4">
            <div className="col-md-6">
              <div className="card account-card fade-in">
                <div className="account-icon"><i className="fas fa-sign-in-alt"></i></div>
                <h3>Existing Customers</h3>
                <p>Access your accounts, manage finances, and utilize our full suite of banking services with our secure online banking platform.</p>
                <Link to="/login" className="btn btn-primary"><i className="fas fa-lock me-2"></i>Secure Login</Link>
              </div>
            </div>

            <div className="col-md-6">
              <div className="card account-card fade-in delay-1">
                <div className="account-icon"><i className="fas fa-user-plus"></i></div>
                <h3>New Customers</h3>
                <p>Open an account in minutes and enjoy our premium banking services with exclusive benefits tailored to your financial goals.</p>
                <a href={`${API_BASE}/main/register`} className="btn btn-primary"><i className="fas fa-rocket me-2"></i>Get Started Now</a>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* About Section */}
      <section className="section section-light" id="about">
        <div className="container">
          <div className="row align-items-center">
            <div className="col-lg-6 mb-5 mb-lg-0">
              <div className="position-relative">
                <img
                  src="https://images.unsplash.com/photo-1600880292203-757bb62b4baf?ixlib=rb-1.2.1&auto=format&fit=crop&w=1350&q=80"
                  className="img-fluid rounded-3 shadow-lg w-100"
                  alt="Our Team"
                />
                <div className="position-absolute bottom-0 end-0 bg-white p-3 rounded shadow-sm" style={{ transform: "translate(20px, 20px)" }}>
                  <div className="d-flex align-items-center">
                    <div className="bg-primary text-white rounded-circle p-3 me-3">
                      <i className="fas fa-award fa-lg"></i>
                    </div>
                    <div>
                      <h5 className="mb-0">Best Digital Bank</h5>
                      <small className="text-muted">2023 Financial Times</small>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div className="col-lg-6">
              <h2 className="section-title">About SysBank</h2>
              <p className="mb-4">
                Founded in 2008, SysBank has grown to become a leading digital banking platform serving over 5 million customers worldwide.
                Our mission is to simplify banking while providing exceptional financial services through innovative technology and personalized solutions.
              </p>

              <div className="row mt-4">
                <div className="col-md-6 mb-4">
                  <div className="card stats-card fade-in">
                    <div className="stats-icon"><i className="fas fa-users"></i></div>
                    <h3 className="stats-number">5M+</h3>
                    <p className="stats-label">Satisfied Customers</p>
                  </div>
                </div>
                <div className="col-md-6 mb-4">
                  <div className="card stats-card fade-in delay-1">
                    <div className="stats-icon"><i className="fas fa-globe"></i></div>
                    <h3 className="stats-number">12</h3>
                    <p className="stats-label">Countries Served</p>
                  </div>
                </div>
                <div className="col-md-6 mb-4">
                  <div className="card stats-card fade-in">
                    <div className="stats-icon"><i className="fas fa-award"></i></div>
                    <h3 className="stats-number">25+</h3>
                    <p className="stats-label">Industry Awards</p>
                  </div>
                </div>
                <div className="col-md-6 mb-4">
                  <div className="card stats-card fade-in delay-1">
                    <div className="stats-icon"><i className="fas fa-hand-holding-usd"></i></div>
                    <h3 className="stats-number">$15B</h3>
                    <p className="stats-label">Assets Managed</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Services Section */}
      <section className="section section-white" id="services">
        <div className="container">
          <div className="text-center mb-5">
            <h2 className="section-title">Our Banking Services</h2>
            <p className="section-subtitle">Comprehensive financial solutions tailored to your personal and business needs</p>
          </div>

          <div className="row g-4">
            <div className="col-md-4">
              <div className="card service-card fade-in">
                <img src="https://images.unsplash.com/photo-1553729459-efe14ef6055d?ixlib=rb-1.2.1&auto=format&fit=crop&w=1350&q=80" className="card-img-top" alt="Account Management" />
                <div className="card-body">
                  <div className="service-icon"><i className="fas fa-wallet"></i></div>
                  <h3>Account Management</h3>
                  <p>Manage all your accounts in one place with our intuitive dashboard and mobile app. Real-time updates and seamless transactions.</p>
                  <a href="#" className="btn btn-outline-primary mt-3">Learn More</a>
                </div>
              </div>
            </div>

            <div className="col-md-4">
              <div className="card service-card fade-in delay-1">
                <img src="https://images.unsplash.com/photo-1582407947304-fd86f028f716?ixlib=rb-1.2.1&auto=format&fit=crop&w=1350&q=80" className="card-img-top" alt="Loan Services" />
                <div className="card-body">
                  <div className="service-icon"><i className="fas fa-hand-holding-usd"></i></div>
                  <h3>Loan Services</h3>
                  <p>Competitive loan options with flexible repayment plans and instant approval. Personal, auto, and mortgage solutions available.</p>
                  <a href="#" className="btn btn-outline-primary mt-3">Learn More</a>
                </div>
              </div>
            </div>

            <div className="col-md-4">
              <div className="card service-card fade-in delay-2">
                <img src="https://images.unsplash.com/photo-1621761191319-c6fb62004040?ixlib=rb-1.2.1&auto=format&fit=crop&w=1350&q=80" className="card-img-top" alt="Investment" />
                <div className="card-body">
                  <div className="service-icon"><i className="fas fa-chart-line"></i></div>
                  <h3>Investment Solutions</h3>
                  <p>Grow your wealth with our expert-curated investment portfolios and advisory services tailored to your risk profile.</p>
                  <a href="#" className="btn btn-outline-primary mt-3">Learn More</a>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Testimonials */}
      <section className="section section-light" id="testimonials">
        <div className="container overflow-hidden">
          <div className="text-center mb-5">
            <h2 className="section-title">What Our Customers Say</h2>
            <p className="section-subtitle">Trusted by thousands of happy customers worldwide</p>
          </div>

          <div className="testimonial-slider">
            <div className="slider-track">
              {TESTIMONIALS.map((t) => (
                <div className="testimonial-card" key={t.name}>
                  <p className="quote">{t.quote}</p>
                  <div className="testimonial-author">
                    <img src={t.img} alt={t.name} />
                    <div className="author-info">
                      <h5>{t.name}</h5>
                      <p>{t.role}</p>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </section>

      {/* Contact Section */}
      <section className="section section-white" id="contact">
        <div className="container">
          <div className="row">
            <div className="col-lg-5 mb-5 mb-lg-0">
              <h2 className="section-title mb-4">Contact Us</h2>
              <p className="mb-4">Have questions or need assistance? Our team is available to help you with all your banking needs.</p>

              <div className="contact-info">
                <div className="d-flex align-items-start mb-4">
                  <i className="fas fa-map-marker-alt mt-1"></i>
                  <div>
                    <h5 className="mb-1">Headquarters</h5>
                    <p>12/5 Bandra Kurla Complex, Mumbai 400070, India</p>
                  </div>
                </div>

                <div className="d-flex align-items-start mb-4">
                  <i className="fas fa-phone-alt mt-1"></i>
                  <div>
                    <h5 className="mb-1">Customer Support</h5>
                    <p>+91 22 1234 5678</p>
                    <p>Available 24/7</p>
                  </div>
                </div>

                <div className="d-flex align-items-start mb-4">
                  <i className="fas fa-envelope mt-1"></i>
                  <div>
                    <h5 className="mb-1">Email Us</h5>
                    <p>support@sysbank.com</p>
                  </div>
                </div>
              </div>
            </div>

            <div className="col-lg-7">
              <div className="contact-form">
                <h3 className="mb-4">Send a Message</h3>
                <form onSubmit={(e) => e.preventDefault()}>
                  <div className="row">
                    <div className="col-md-6 mb-3">
                      <label className="form-label">Full Name <span className="text-danger">*</span></label>
                      <input type="text" className="form-control" placeholder="Your Name" required />
                    </div>
                    <div className="col-md-6 mb-3">
                      <label className="form-label">Email Address <span className="text-danger">*</span></label>
                      <input type="email" className="form-control" placeholder="your@email.com" required />
                    </div>
                  </div>

                  <div className="mb-3">
                    <label className="form-label">Subject <span className="text-danger">*</span></label>
                    <input type="text" className="form-control" placeholder="Subject" required />
                  </div>

                  <div className="mb-3">
                    <label className="form-label">Message <span className="text-danger">*</span></label>
                    <textarea className="form-control" rows="5" placeholder="Your Message" required></textarea>
                  </div>

                  <button type="submit" className="btn btn-primary w-100 py-3">Send Message</button>
                </form>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="footer">
        <div className="container">
          <div className="row">
            <div className="col-lg-4 mb-5 mb-lg-0">
              <a href="#" className="footer-logo">
                <i className="fas fa-university"></i>SysBank
              </a>
              <p className="footer-about">SysBank is a leading digital banking platform committed to providing secure, innovative, and customer-centric financial solutions to individuals and businesses worldwide.</p>

              <div className="social-links mt-4">
                <a href="#"><i className="fab fa-facebook-f"></i></a>
                <a href="#"><i className="fab fa-twitter"></i></a>
                <a href="#"><i className="fab fa-linkedin-in"></i></a>
                <a href="#"><i className="fab fa-instagram"></i></a>
                <a href="#"><i className="fab fa-youtube"></i></a>
              </div>
            </div>

            <div className="col-lg-2 col-md-4 mb-4 mb-md-0">
              <div className="footer-links">
                <h5>Quick Links</h5>
                <ul>
                  <li><a href="#">Home</a></li>
                  <li><a href="#about">About Us</a></li>
                  <li><a href="#services">Services</a></li>
                  <li><a href="#testimonials">Testimonials</a></li>
                  <li><a href="#contact">Contact</a></li>
                </ul>
              </div>
            </div>

            <div className="col-lg-3 col-md-4 mb-4 mb-md-0">
              <div className="footer-links">
                <h5>Services</h5>
                <ul>
                  <li><a href="#">Personal Banking</a></li>
                  <li><a href="#">Business Accounts</a></li>
                  <li><a href="#">Loans &amp; Mortgages</a></li>
                  <li><a href="#">Investment Plans</a></li>
                  <li><a href="#">Credit Cards</a></li>
                </ul>
              </div>
            </div>

            <div className="col-lg-3 col-md-4">
              <div className="footer-links">
                <h5>Newsletter</h5>
                <p>Subscribe to our newsletter for the latest updates and offers.</p>
                <form className="mt-3" onSubmit={(e) => e.preventDefault()}>
                  <div className="input-group mb-3">
                    <input type="email" className="form-control" placeholder="Your Email" required />
                    <button className="btn btn-primary" type="submit">
                      <i className="fas fa-paper-plane"></i>
                    </button>
                  </div>
                  <small className="text-muted">We&apos;ll never share your email with anyone else.</small>
                </form>
              </div>
            </div>
          </div>

          <div className="footer-bottom text-center">
            <p className="mb-0">&copy; 2023 SysBank. All rights reserved. | <a href="#">Privacy Policy</a> | <a href="#">Terms of Service</a> | <a href="#">Security</a></p>
          </div>
        </div>
      </footer>
    </div>
  );
}

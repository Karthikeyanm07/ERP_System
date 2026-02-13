/**
 * Privacy & Policy Page
 *
 * Static privacy policy page accessible from:
 * - User dropdown menu in Navbar
 * - Login page footer
 * - /privacy-policy route (public)
 */

import { Link, useNavigate } from "react-router-dom";
import { ArrowLeft, Shield, Eye, Lock, Database, Users, Globe, Mail } from "lucide-react";

const Section = ({ icon: Icon, title, children }) => (
  <div className="mb-8">
    <div className="flex items-center gap-3 mb-3">
      <div className="flex items-center justify-center w-8 h-8 rounded-lg bg-blue-50 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400">
        <Icon size={16} />
      </div>
      <h2 className="text-lg font-semibold text-gray-900 dark:text-gray-100">{title}</h2>
    </div>
    <div className="pl-11 text-sm text-gray-600 dark:text-gray-400 leading-relaxed space-y-3">
      {children}
    </div>
  </div>
);

const PrivacyPolicy = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-950">
      {/* Header */}
      <header className="sticky top-0 z-10 bg-white/80 dark:bg-gray-900/80 backdrop-blur-md border-b border-gray-200 dark:border-gray-800">
        <div className="max-w-3xl mx-auto px-6 py-4 flex items-center gap-4">
          <button
            onClick={() => navigate(-1)}
            className="p-2 text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg transition-colors"
            aria-label="Go back"
          >
            <ArrowLeft size={20} />
          </button>
          <div>
            <h1 className="text-xl font-bold text-gray-900 dark:text-gray-100">Privacy & Policy</h1>
            <p className="text-xs text-gray-500 dark:text-gray-400 mt-0.5">
              Last updated: February 2026
            </p>
          </div>
        </div>
      </header>

      {/* Content */}
      <main className="max-w-3xl mx-auto px-6 py-10">
        {/* Introduction */}
        <div className="mb-10 p-6 bg-blue-50 dark:bg-blue-900/10 border border-blue-100 dark:border-blue-900/30 rounded-xl">
          <div className="flex items-start gap-3">
            <Shield size={20} className="text-blue-600 dark:text-blue-400 mt-0.5 flex-shrink-0" />
            <div>
              <p className="text-sm text-gray-700 dark:text-gray-300 leading-relaxed">
                This Privacy Policy describes how your organization's ERP system
                collects, uses, and protects information. As an enterprise
                application, this system is operated by your organization and is
                subject to your company's data governance policies.
              </p>
            </div>
          </div>
        </div>

        {/* Sections */}
        <Section icon={Database} title="Information We Collect">
          <p>
            The ERP system collects and processes business-related data necessary for
            organizational operations, including:
          </p>
          <ul className="list-disc pl-5 space-y-1.5">
            <li><strong>Employee Data:</strong> Names, contact details, department assignments, attendance records, and leave history.</li>
            <li><strong>Financial Data:</strong> Account information, transactions, expenses, invoices, and payment records.</li>
            <li><strong>Inventory Data:</strong> Product catalogs, stock levels, purchase orders, and supplier information.</li>
            <li><strong>Sales Data:</strong> Customer profiles, sales orders, invoices, and payment histories.</li>
            <li><strong>System Data:</strong> User credentials (encrypted), login timestamps, and audit logs.</li>
          </ul>
        </Section>

        <Section icon={Eye} title="How We Use Your Data">
          <p>Your data is used exclusively for business operations within your organization:</p>
          <ul className="list-disc pl-5 space-y-1.5">
            <li>Managing HR processes including payroll, attendance, and leave management.</li>
            <li>Processing financial transactions, generating reports, and maintaining records.</li>
            <li>Tracking inventory, managing supply chains, and fulfilling orders.</li>
            <li>Providing audit trails and compliance reporting.</li>
            <li>Authenticating users and managing role-based access control.</li>
          </ul>
        </Section>

        <Section icon={Lock} title="Data Protection & Security">
          <p>We implement industry-standard security measures to protect your data:</p>
          <ul className="list-disc pl-5 space-y-1.5">
            <li><strong>Encryption:</strong> All passwords are hashed using BCrypt. Data in transit is encrypted via HTTPS/TLS.</li>
            <li><strong>Authentication:</strong> JWT-based token authentication with configurable expiration.</li>
            <li><strong>Authorization:</strong> Role-based access control (RBAC) ensures users only access permitted resources.</li>
            <li><strong>Audit Logging:</strong> All critical actions are logged with timestamps and user identification.</li>
            <li><strong>Session Management:</strong> Automatic session timeout and secure token handling.</li>
          </ul>
        </Section>

        <Section icon={Users} title="Data Access & Sharing">
          <p>
            Access to data within the ERP system is governed by role-based permissions
            configured by your organization's administrator:
          </p>
          <ul className="list-disc pl-5 space-y-1.5">
            <li><strong>Admin users</strong> have full access to all modules and can manage user permissions.</li>
            <li><strong>Manager users</strong> have access to their department's data and reporting tools.</li>
            <li><strong>Standard users</strong> can only view and interact with data relevant to their role.</li>
          </ul>
          <p className="mt-3">
            Data is not shared with any third parties. All data remains within your
            organization's deployment environment.
          </p>
        </Section>

        <Section icon={Globe} title="Data Retention">
          <p>
            Data retention policies are determined by your organization. The ERP system
            supports:
          </p>
          <ul className="list-disc pl-5 space-y-1.5">
            <li>Configurable data retention periods per module.</li>
            <li>Archival of historical records for compliance requirements.</li>
            <li>Secure deletion of records upon authorized request.</li>
            <li>Database backup and recovery procedures as configured by your IT team.</li>``
          </ul>
        </Section>

        <Section icon={Mail} title="Contact & Updates">
          <p>
            For questions about this privacy policy or data handling practices, contact
            your organization's system administrator or IT department.
          </p>
          <p>
            This policy may be updated periodically to reflect changes in data
            practices or regulatory requirements. Users will be notified of
            significant changes through the system.
          </p>
        </Section>

        {/* Footer */}
        <div className="mt-12 pt-8 border-t border-gray-200 dark:border-gray-800 text-center">
          <p className="text-xs text-gray-400 dark:text-gray-500">
            © {new Date().getFullYear()} ERP System. All rights reserved.
          </p>
          <p className="text-xs text-gray-400 dark:text-gray-500 mt-1">
            This privacy policy applies to your organization's deployment of the ERP system.
          </p>
        </div>
      </main>
    </div>
  );
};

export default PrivacyPolicy;

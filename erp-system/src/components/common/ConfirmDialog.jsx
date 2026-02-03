/**
 * Confirmation Dialog Component
 *
 * A reusable modal for confirming destructive actions
 * Better UX than window.confirm()
 */

import Modal from "./Modal";
import Button from "./Button";
import { AlertTriangle } from "lucide-react";

const ConfirmDialog = ({
  isOpen,
  onClose,
  onConfirm,
  title = "Confirm Action",
  message = "Are you sure you want to proceed?",
  confirmText = "Confirm",
  cancelText = "Cancel",
  variant = "danger", // 'danger' or 'warning'
  loading = false,
}) => {
  const handleConfirm = () => {
    onConfirm();
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={title}
      size="sm"
      footer={
        <>
          <Button variant="outline" onClick={onClose} disabled={loading}>
            {cancelText}
          </Button>
          <Button
            variant={variant === "danger" ? "danger" : "primary"}
            onClick={handleConfirm}
            loading={loading}
          >
            {confirmText}
          </Button>
        </>
      }
    >
      <div className="flex items-start gap-4">
        <div
          className={`flex-shrink-0 w-12 h-12 rounded-full flex items-center justify-center ${
            variant === "danger"
              ? "bg-red-100 dark:bg-red-500/30"
              : "bg-yellow-100 dark:bg-yellow-500/30"
          }`}
        >
          <AlertTriangle
            size={24}
            className={
              variant === "danger"
                ? "text-red-600 dark:text-red-300"
                : "text-yellow-600 dark:text-yellow-300"
            }
          />
        </div>
        <div className="flex-1">
          <p className="text-gray-700 dark:text-gray-200 leading-relaxed">
            {message}
          </p>
        </div>
      </div>
    </Modal>
  );
};

export default ConfirmDialog;

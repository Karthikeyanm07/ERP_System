import { useState } from "react";
import { useApi } from "./useApi";
import { useToast } from "../components/common/Toast";
import { logger } from "../utils/logger";

/**
 * useCrudForm - A reusable hook for Entity Management (CRUD)
 * 
 * @param {Object} options Configuration options
 * @param {Object} options.initialData Default form state
 * @param {Function} options.validate Validation logic
 * @param {Object} options.api API object with create/update/delete/getAll methods
 * @param {String} options.entityName Human-readable name of the entity
 * @param {Function} options.onSuccess Callback after successful mutation
 */
export const useCrudForm = ({
  initialData,
  validate,
  api,
  entityName,
  onSuccess
}) => {
  const { execute, loading } = useApi();
  const toast = useToast();
  
  const [items, setItems] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [itemToDelete, setItemToDelete] = useState(null);
  const [editingItem, setEditingItem] = useState(null);
  const [formLoading, setFormLoading] = useState(false);
  const [errors, setErrors] = useState({});
  const [formData, setFormData] = useState(initialData);

  const fetchItems = async () => {
    try {
      const data = await execute(api[`get${entityName}s`] || api.getAll);
      setItems(Array.isArray(data) ? data : []);
    } catch (error) {
      logger.error(`Error fetching ${entityName}s`, error);
      toast.error(`Failed to load ${entityName}s`);
      setItems([]);
    }
  };

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }));
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: "" }));
    }
  };

  const handleAdd = () => {
    setEditingItem(null);
    setFormData(initialData);
    setErrors({});
    setIsModalOpen(true);
  };

  const handleEdit = (item) => {
    setEditingItem(item);
    setFormData(item);
    setErrors({});
    setIsModalOpen(true);
  };

  const handleDelete = (item) => {
    setItemToDelete(item);
    setIsDeleteDialogOpen(true);
  };

  const confirmDelete = async () => {
    if (!itemToDelete) return;
    try {
      await api[`delete${entityName}`](itemToDelete.id);
      toast.success(`${entityName} deleted successfully`);
      await fetchItems();
      setIsDeleteDialogOpen(false);
      setItemToDelete(null);
      if (onSuccess) onSuccess();
    } catch (error) {
      toast.error(error.response?.data?.message || `Error deleting ${entityName}`);
    }
  };

  const handleSubmit = async (e) => {
    if (e) e.preventDefault();

    const validationErrors = validate ? validate(formData) : {};
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      toast.warning("Please fix the form errors");
      return;
    }

    setFormLoading(true);
    try {
      if (editingItem) {
        await api[`update${entityName}`](editingItem.id, formData);
        toast.success(`${entityName} updated successfully`);
      } else {
        await api[`create${entityName}`](formData);
        toast.success(`${entityName} created successfully`);
      }
      await fetchItems();
      setIsModalOpen(false);
      if (onSuccess) onSuccess();
    } catch (error) {
      logger.error(`Error saving ${entityName}`, error);
      toast.error(error.response?.data?.message || `Error saving ${entityName}`);
    } finally {
      setFormLoading(false);
    }
  };

  return {
    items,
    setItems,
    formData,
    setFormData,
    errors,
    loading,
    formLoading,
    isModalOpen,
    setIsModalOpen,
    isDeleteDialogOpen,
    setIsDeleteDialogOpen,
    editingItem,
    itemToDelete,
    fetchItems,
    handleChange,
    handleAdd,
    handleEdit,
    handleDelete,
    confirmDelete,
    handleSubmit,
  };
};

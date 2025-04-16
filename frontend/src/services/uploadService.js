import axios from 'axios';

export const handleUploadFile = async (file, setProgress) => {
    const formData = new FormData();
    formData.append('file', file);

    const isLatex = file.name.toLowerCase().endsWith('.tex');
    const uploadUrl = isLatex
        ? 'http://localhost:5000/api/validate/latex'
        : 'http://localhost:5000/api/validate/docx';


    const config = {
        headers: { 'Content-Type': 'multipart/form-data' },
        onUploadProgress: (event) => {
            const percent = Math.round((event.loaded / event.total) * 100);
            setProgress(percent);
        },
    };

    try {
        const response = await axios.post(uploadUrl, formData, config);
        return response.data;
    } catch (error) {
        console.error("File upload error:", error);
        throw error;
    }
};

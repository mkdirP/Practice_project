import axios from 'axios';

export const handleUploadFile = async (file, setProgress) => {
    const formData = new FormData();
    formData.append('file', file);

    const config = {
        headers: { 'Content-Type': 'multipart/form-data' },
        onUploadProgress: (event) => {
            const percent = Math.round((event.loaded / event.total) * 100);
            setProgress(percent);
        },
    };

    const response = await axios.post('http://localhost:8080/api/validate', formData, config);
    return response.data;
};

import React, { useState } from 'react';
import {Spin, Progress, message, Button} from 'antd';
import FileUploader from './components/FileUploader';
import ValidationTable from './components/ValidationTable';
import StatsChart from './components/StatsChart';
import { handleUploadFile } from './services/uploadService';
import { generatePDFReport } from './utils/PdfGenerator';
import './styles/demo.css';

const App = () => {
    const [fileList, setFileList] = useState([]);
    const [validationResult, setValidationResult] = useState([]);
    const [loading, setLoading] = useState(false);
    const [uploadProgress, setUploadProgress] = useState(0);
    const [uploadError, setUploadError] = useState(null);
    const [stats, setStats] = useState({});

    const handleUpload = async (file) => {
        setLoading(true);
        setUploadProgress(0);
        try {
            const data = await handleUploadFile(file, setUploadProgress);
            setValidationResult(data.messages);
            setStats(data.stats.errorTypeCount);
        } catch (err) {
            message.error('上传失败，请重试');
        } finally {
            setLoading(false);
        }
    };

    const handleRemoveFile = () => {
        setFileList([]);
        setValidationResult([]);
        setStats({});
        setUploadError(null);
    };

    return (
        <div className="container">
            <h1 className="page-title">自动检查模板</h1>

            <FileUploader
                fileList={fileList}
                setFileList={setFileList}
                onUpload={handleUpload}
                onRemove={handleRemoveFile}
                uploadError={uploadError}
                setUploadError={setUploadError}
            />

            {loading && (
                <div className="loading-indicator">
                    <Spin size="large" />
                    <Progress percent={uploadProgress} size="small" className="progress-bar" />
                </div>
            )}

            {validationResult.length > 0 && (
                <>
                    <ValidationTable data={validationResult} />
                    <div className="download-section">
                        <Button
                            type="primary"
                            className="download-btn"
                            onClick={() => generatePDFReport(validationResult)}
                        >
                            下载PDF报告
                        </Button>
                    </div>
                </>
            )}

            {Object.keys(stats).length > 0 && (
                <StatsChart stats={stats} />
            )}
        </div>
    );
};

export default App;

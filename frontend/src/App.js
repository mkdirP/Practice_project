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

            // 检查 data 是否存在并且包含 messages 和 stats
            if (data && data.messages && data.stats) {
                setValidationResult(data.messages);
                setStats(data.stats.errorTypeCount);
            } else {
                // 如果没有返回有效的 messages 或 stats
                message.error('返回的数据格式不正确');
            }

            setUploadProgress(100);
        } catch (err) {
            console.error(err);
            message.error('Загрузка не удалась, попробуйте еще раз');
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
            <h1 className="page-title">Авторизация проверки шаблона ВКР</h1>

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
                            Загрузить отчет в формате PDF
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

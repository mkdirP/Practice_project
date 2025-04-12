import React, { useState } from 'react';
import { jsPDF } from "jspdf";
import { autoTable } from "jspdf-autotable";
import { Upload, Button, Table, Spin, Progress, message, Typography } from 'antd';
import { UploadOutlined, DownloadOutlined, DeleteOutlined } from '@ant-design/icons';
import axios from 'axios';
import './demo.css';
import "./ofont.ru_Times New Roman-normal";

const { Text } = Typography;

const App = () => {
    const [fileList, setFileList] = useState([]);
    const [validationResult, setValidationResult] = useState([]);
    const [loading, setLoading] = useState(false);
    const [uploadProgress, setUploadProgress] = useState(0);
    const [uploadError, setUploadError] = useState(null); // 新增错误信息状态

    // 文件上传前的检查
    const beforeUpload = (file) => {
        const isWord = file.type === 'application/vnd.openxmlformats-officedocument.wordprocessingml.document';
        const isPdf = file.type === 'application/pdf';
        const isLt5M = file.size / 1024 / 1024 < 5;

        if (!isWord && !isPdf) {
            setUploadError('只能上传Word或PDF文件！');
            return false;
        }

        if (!isLt5M) {
            setUploadError('文件大小必须小于 5MB！');
            return false;
        }

        setUploadError(null); // 清除错误信息
        return true;
    };

    // 文件上传处理
    const handleUpload = async (file) => {
        setLoading(true);
        setUploadProgress(0); // 重置进度条
        const formData = new FormData();
        formData.append('file', file);

        try {
            // 模拟文件上传进度
            const config = {
                headers: { 'Content-Type': 'multipart/form-data' },
                onUploadProgress: (progressEvent) => {
                    const percent = Math.round((progressEvent.loaded / progressEvent.total) * 100);
                    setUploadProgress(percent);
                },
            };

            const response = await axios.post('http://localhost:8080/api/validate', formData, config);
            setValidationResult(response.data);
            setLoading(false);
            setUploadProgress(100);
        } catch (error) {
            message.error('上传失败，请重试');
            setLoading(false);
            setUploadProgress(0);
        }
    };

    // 删除已上传文件
    const handleRemoveFile = () => {
        setFileList([]);
        setValidationResult([]);
        setUploadError(null); // 清除错误信息
    };

    // 列表显示的字段
    const columns = [
        {
            title: '错误类型',
            dataIndex: 'type',
        },
        {
            title: '错误信息',
            dataIndex: 'message',
        },
        {
            title: '建议',
            dataIndex: 'suggestion',
        },
        {
            title: '错误上下文',
            dataIndex: 'context',
            ellipsis: true,
            render: (text) => (
                <div style={{ maxWidth: '200px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                    {text}
                </div>
            ),
        },
    ];

    // 根据错误类型高亮
    const getRowClassName = (record) => {
        if (record.type === 'FontSizeMismatch') {
            return 'error-font-size';
        }
        if (record.type === 'AlignmentError') {
            return 'error-alignment';
        }
        return '';
    };

    const handleDownloadPDFReport = () => {
        const doc = new jsPDF();

        // 设置字体
        doc.setFont("ofont.ru_Times New Roman", "normal");
        doc.setFontSize(12);

        // 标题
        doc.text("Отчет о проверке шаблона ВКР", 20, 20);

        // 添加表头
        const headers = ["错误类型", "错误信息", "建议", "错误上下文"];
        const tableData = validationResult.map((item) => [
            item.type,
            item.message,
            item.suggestion,
            item.context,
        ]);

        // 设置表格样式
        const startY = 30; // 表格开始的Y坐标
        autoTable(doc,{
            head: [headers],
            body: tableData,
            startY,
            styles: {
                font: "ofont.ru_Times New Roman",        // ✅ 明确设置字体名
                fontStyle: "normal",  // ✅ 与 setFont 中一致
                fontSize: 10,
                cellPadding: 3,
            },
        });

        // 下载PDF
        doc.save("validation_report.pdf");
    };

    return (
        <div className="container">
            <h1 className="page-title">自动检查模板</h1>
            <div className="upload-section">
                <Upload
                    beforeUpload={beforeUpload}
                    customRequest={({ file, onSuccess }) => {
                        setFileList([file]); // 设置上传的文件
                        handleUpload(file);
                        onSuccess();
                    }}
                    showUploadList={false}
                    fileList={fileList}
                >
                    <Button icon={<UploadOutlined />} size="large" className="upload-btn">
                        点击上传ВКР模板
                    </Button>
                </Upload>
                {fileList.length > 0 && (
                    <div className="file-info">
                        <div className="file-info-inner">
                            <span className="file-name">{fileList[0].name}</span>
                            <Button
                                icon={<DeleteOutlined />}
                                size="small"
                                type="text"
                                onClick={handleRemoveFile}
                                className="delete-btn"
                            >
                                删除文件
                            </Button>
                        </div>
                    </div>
                )}

                {/* 显示错误信息 */}
                {uploadError && (
                    <div className="upload-error">
                        <Text type="danger">{uploadError}</Text>
                    </div>
                )}
            </div>

            <div className="loading-indicator">
                {loading && (
                    <>
                        <Spin size="large" />
                        <Progress percent={uploadProgress} size="small" className="progress-bar" />
                    </>
                )}
            </div>

            {validationResult.length > 0 && (
                <div className="results-section">
                    <Table
                        columns={columns}
                        dataSource={validationResult}
                        pagination={{
                            showSizeChanger: true, // 允许用户选择每页条数
                            pageSizeOptions: ['5', '10', '20', '50'], // 可选的每页条数
                            defaultPageSize: 5,
                            showQuickJumper: true, // 允许用户快速跳页
                        }}
                        rowKey={(record, index) => index}
                        bordered
                        rowClassName={getRowClassName}
                        scroll={{ x: 'max-content' }}
                        className="results-table"
                    />
                </div>
            )}

            {/*{validationResult.length > 0 && (*/}
            {/*    <div className="download-section">*/}
            {/*        <Button*/}
            {/*            type="primary"*/}
            {/*            icon={<DownloadOutlined />}*/}
            {/*            onClick={handleDownloadReport}*/}
            {/*            className="download-btn"*/}
            {/*        >*/}
            {/*            下载报告*/}
            {/*        </Button>*/}
            {/*    </div>*/}
            {/*)}*/}
            {validationResult.length > 0 && (
                <div className="download-section">
                    <Button
                        type="primary"
                        icon={<DownloadOutlined />}
                        onClick={handleDownloadPDFReport}
                        className="download-btn"
                    >
                        下载PDF报告
                    </Button>
                </div>
            )}

        </div>
    );
};

export default App;

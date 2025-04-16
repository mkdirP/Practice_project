import React from 'react';
import { Upload, Button, Typography } from 'antd';
import { UploadOutlined, DeleteOutlined } from '@ant-design/icons';

const { Text } = Typography;

const FileUploader = ({
                          fileList,
                          setFileList,
                          onUpload,
                          onRemove,
                          uploadError,
                          setUploadError
                      }) => {

    const beforeUpload = (file) => {
        const isWord = file.type === 'application/vnd.openxmlformats-officedocument.wordprocessingml.document';
        // const isPdf = file.type === 'application/pdf';
        const isLatex = file.name.toLowerCase().endsWith('.tex');
        const isLt5M = file.size / 1024 / 1024 < 5;

        if (!isWord && !isLatex) {
            setUploadError('Можно загружать только файлы .docx или .tex!');
            return false;
        }

        if (!isLt5M) {
            setUploadError('Размер файла должен быть менее 5 МБ!');
            return false;
        }

        setUploadError(null);
        return true;
    };

    return (
        <div className="upload-section">
            <Upload
                beforeUpload={beforeUpload}
                customRequest={({ file, onSuccess }) => {
                    setFileList([file]);
                    onUpload(file);
                    onSuccess();
                }}
                showUploadList={false}
                fileList={fileList}
            >
                <Button icon={<UploadOutlined />} size="large" className="upload-btn">
                    Нажмите, чтобы загрузить шаблон ВКР
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
                            onClick={onRemove}
                            className="delete-btn"
                        >
                        </Button>
                    </div>
                </div>
            )}

            {uploadError && (
                <div className="upload-error">
                    <Text type="danger">{uploadError}</Text>
                </div>
            )}
        </div>
    );
};

export default FileUploader;

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
                            onClick={onRemove}
                            className="delete-btn"
                        >
                            删除文件
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

import React from 'react';
import { Table } from 'antd';

const ValidationTable = ({ data }) => {
    const columns = [
        {
            title: 'Тип ошибки',
            dataIndex: 'code',
        },
        {
            title: 'Сообщение об ошибке',
            dataIndex: 'message',
        },
        {
            title: 'Предложение',
            dataIndex: 'suggestion',
        },
        {
            title: 'Контекст ошибки',
            dataIndex: 'content',
            ellipsis: true,
            render: (text) => (
                <div style={{ maxWidth: '200px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                    {text}
                </div>
            ),
        },
    ];

    const getRowClassName = (record) => {
        if (record.code === 'FontSizeMismatch') return 'error-font-size';
        if (record.code === 'AlignmentError') return 'error-alignment';
        if (record.code === 'CenterAlignmentError') return 'error-centerAlignment';
        if (record.code === 'ContentMismatch') return 'error-contentMismatch';
        if (record.code === 'BoldError') return 'error-boldError';
        if (record.code === 'MissingTOC') return 'error-missingTOC';
        if (record.code === 'MissingSection') return 'error-missingSection';
        if (record.code === 'TOCNotOnNewPageBefore') return 'error-TOCNotOnNewPageBefore';
        if (record.code === 'TOCNotOnNewPageAfter') return 'error-TOCNotOnNewPageAfter';
        return '';
    };

    return (
        <div className="results-section">
            <Table
                columns={columns}
                dataSource={data}
                pagination={{
                    showSizeChanger: true,
                    pageSizeOptions: ['5', '10', '20', '50'],
                    defaultPageSize: 5,
                    showQuickJumper: true,
                }}
                rowKey={(record, index) => index}
                bordered
                rowClassName={getRowClassName}
                scroll={{ x: 'max-content' }}
                className="results-table"
            />
        </div>
    );
};

export default ValidationTable;

import React from 'react';
import { Table } from 'antd';

const ValidationTable = ({ data }) => {
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

    const getRowClassName = (record) => {
        if (record.type === 'FontSizeMismatch') return 'error-font-size';
        if (record.type === 'AlignmentError') return 'error-alignment';
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

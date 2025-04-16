import React from 'react';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts';

const StatsChart = ({ stats }) => {
    const data = Object.entries(stats).map(([type, count]) => ({
        type,
        count,
    }));

    return (
        <div className="stats-section">
            <h3>Статистика типов ошибок</h3>
            <ResponsiveContainer width="100%" height={300}>
                <BarChart
                    data={data}
                    margin={{ top: 20, right: 30, left: 0, bottom: 5 }}
                >
                    <XAxis dataKey="type" />
                    <YAxis />
                    <Tooltip />
                    <Bar dataKey="count" fill="#1890ff" />
                </BarChart>
            </ResponsiveContainer>
        </div>
    );
};

export default StatsChart;

// import React, { useState, useEffect } from 'react';
// import './FinancialAccount.css';

// const FinancialAccount = () => {
//     const [financialData, setFinancialData] = useState({
//         tuitionFee: 0,
//     });

//     const [paymentAmount, setPaymentAmount] = useState('');
//     const [statusMessage, setStatusMessage] = useState('');

//     const fetchFinancialData = async () => {
//         try {
//             const response = await fetch('/getFinancialData', {
//                 method: 'GET',
//                 credentials: 'include',
//                 headers: {
//                     'Content-Type': 'application/json',
//                 },
//             });

//             if (!response.ok) {
//                 throw new Error(`HTTP error! Status: ${response.status}`);
//             }

//             const data = await response.json();

//             // Access tuitionFee, not tuition
//             if (data.tuitionFee === undefined) {
//                 throw new Error('Invalid data structure');
//             }

//             setFinancialData(data);
//         } catch (error) {
//             console.error('Error fetching financial data:', error);
//             setStatusMessage('Failed to fetch financial data');
//         }
//     };

//     useEffect(() => {
//         fetchFinancialData();
//     }, []);

//     const handlePayment = async () => {
//         const amount = parseFloat(paymentAmount);
    
//         if (isNaN(amount) || amount <= 0 || amount > financialData.tuitionFee) {
//             setStatusMessage('Invalid payment amount. Please check and try again.');
//             return;
//         }
    
//         try {
//             const response = await fetch('/makePayment', {
//                 method: 'POST',
//                 headers: {
//                     'Content-Type': 'application/json',
//                 },
//                 body: JSON.stringify({ amount }),
//                 credentials: 'include',
//             });
    
//             if (!response.ok) {
//                 throw new Error('Payment failed!');
//             }
    
//             const updatedData = await response.json();
//             setFinancialData(updatedData);
//             setPaymentAmount('');
//             setStatusMessage(`Payment of $${amount} was successful!`);
//         } catch (error) {
//             console.error('Error processing payment:', error);
//             setStatusMessage('Payment failed. Please try again later.');
//         }
//     };
    

//     return (
//         <div className="container financial-account py-5" id="financial-account">
//             <h1 className="text-center text-primary mb-5">Financial Account</h1>

//             <div className="tuition-fee-container mb-5">
//                 <h3 className="tuition-fee-title">Tuition Fee: ${financialData.tuitionFee}</h3>
//             </div>

//             <div className="payment-section mb-5">
//                 <h4>Make a Payment</h4>
//                 <input
//                     type="number"
//                     className="form-control"
//                     placeholder="Enter amount to pay"
//                     value={paymentAmount}
//                     onChange={(e) => setPaymentAmount(e.target.value)}
//                     min="0"
//                     step="any"
//                 />
//                 <button className="btn btn-primary mt-3" onClick={handlePayment}>
//                     Pay Now
//                 </button>
//             </div>

//             {statusMessage && <div className="status-message text-center mt-4">{statusMessage}</div>}
//         </div>
//     );
// };

// export default FinancialAccount;

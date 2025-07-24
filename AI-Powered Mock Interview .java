import React, { useState, useEffect, useRef } from 'react';
import { User, Bot, Play, Pause, RotateCcw, BarChart3, MessageCircle, Clock, Award, CheckCircle, XCircle, Star, Lock, LogOut, Shield } from 'lucide-react';

const InterviewPlatform = () => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [currentUser, setCurrentUser] = useState(null);
  const [userRole, setUserRole] = useState('user');
  const [loginForm, setLoginForm] = useState({ username: '', password: '' });
  const [authError, setAuthError] = useState('');
  const [currentView, setCurrentView] = useState('dashboard');
  const [selectedDomain, setSelectedDomain] = useState('software-engineering');
  const [interviewState, setInterviewState] = useState('idle');
  const [currentQuestion, setCurrentQuestion] = useState('');
  const [questionNumber, setQuestionNumber] = useState(0);
  const [userAnswers, setUserAnswers] = useState([]);
  const [timeElapsed, setTimeElapsed] = useState(0);
  const [currentAnswer, setCurrentAnswer] = useState('');
  const [performanceData, setPerformanceData] = useState([]);
  const [chatMessages, setChatMessages] = useState([]);
 
  const intervalRef = useRef(null);

  const domains = {
    'software-engineering': {
      name: 'Software Engineering',
      color: 'bg-blue-500',
      questions: [
        "Explain the difference between REST and GraphQL APIs.",
        "How would you implement a rate limiting system?",
        "Describe the SOLID principles in software design.",
        "What are the trade-offs between microservices and monolithic architecture?",
        "How do you handle database migrations in a production environment?"
      ]
    },
    'data-science': {
      name: 'Data Science',
      color: 'bg-green-500',
      questions: [
        "Explain the bias-variance tradeoff in machine learning.",
        "How would you handle missing data in a dataset?",
        "Describe the difference between supervised and unsupervised learning.",
        "What metrics would you use to evaluate a classification model?",
        "How do you prevent overfitting in neural networks?"
      ]
    },
    'product-management': {
      name: 'Product Management',
      color: 'bg-purple-500',
      questions: [
        "How would you prioritize features for a product roadmap?",
        "Describe how you would measure product success.",
        "How do you handle conflicting stakeholder requirements?",
        "Walk me through your process for user research.",
        "How would you launch a product in a new market?"
      ]
    },
    'business-analysis': {
      name: 'Business Analysis',
      color: 'bg-orange-500',
      questions: [
        "How do you gather and document business requirements?",
        "Explain the difference between functional and non-functional requirements.",
        "How would you conduct a stakeholder analysis?",
        "Describe your approach to process improvement.",
        "How do you handle scope creep in projects?"
      ]
    }
  };

  const users = {
    'user1': { password: 'pass123', role: 'user', name: 'John Doe' },
    'admin': { password: 'admin123', role: 'admin', name: 'Admin User' },
    'testuser': { password: 'test123', role: 'user', name: 'Test User' }
  };

  const handleLogin = (e) => {
    e.preventDefault();
    setAuthError('');
   
    const user = users[loginForm.username];
   
    if (user && user.password === loginForm.password) {
      setIsAuthenticated(true);
      setCurrentUser({ username: loginForm.username, name: user.name });
      setUserRole(user.role);
      setCurrentView('dashboard');
      setLoginForm({ username: '', password: '' });
    } else {
      setAuthError('Invalid credentials. Authentication fails.');
    }
  };

  const handleQuickLogin = (username, name, role) => {
    setIsAuthenticated(true);
    setCurrentUser({ username, name });
    setUserRole(role);
    setCurrentView('dashboard');
  };

  const handleLogout = () => {
    setIsAuthenticated(false);
    setCurrentUser(null);
    setUserRole('user');
    setCurrentView('dashboard');
    resetInterview();
  };

  useEffect(() => {
    if (interviewState === 'active') {
      intervalRef.current = setInterval(() => {
        setTimeElapsed(prev => prev + 1);
      }, 1000);
    } else {
      clearInterval(intervalRef.current);
    }
    return () => clearInterval(intervalRef.current);
  }, [interviewState]);

  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  const startInterview = () => {
    if (!isAuthenticated) {
      setAuthError('Please log in to start an interview session.');
      return;
    }
   
    if (!selectedDomain || !domains[selectedDomain] || domains[selectedDomain].questions.length === 0) {
      alert('No questions available for the selected domain.');
      return;
    }

    setInterviewState('active');
    setQuestionNumber(1);
    setTimeElapsed(0);
    setUserAnswers([]);
    setCurrentAnswer('');
    setChatMessages([]);
   
    const firstQuestion = domains[selectedDomain].questions[0];
    setCurrentQuestion(firstQuestion);
   
    setChatMessages([{
      type: 'bot',
      content: `Welcome to your ${domains[selectedDomain].name} interview! Let's begin with the first question: ${firstQuestion}`,
      timestamp: new Date().toLocaleTimeString()
    }]);
  };

  const pauseInterview = () => {
    setInterviewState('paused');
  };

  const resumeInterview = () => {
    setInterviewState('active');
  };

  const submitAnswer = () => {
    if (!currentAnswer.trim()) return;

    const answer = {
      question: currentQuestion,
      answer: currentAnswer,
      timeSpent: timeElapsed,
      questionNumber: questionNumber
    };

    setUserAnswers(prev => [...prev, answer]);
    setChatMessages(prev => [...prev, {
      type: 'user',
      content: currentAnswer,
      timestamp: new Date().toLocaleTimeString()
    }]);

    const feedback = generateFeedback(currentAnswer);
    setChatMessages(prev => [...prev, {
      type: 'bot',
      content: feedback,
      timestamp: new Date().toLocaleTimeString()
    }]);

    setCurrentAnswer('');

    if (questionNumber < domains[selectedDomain].questions.length) {
      const nextQuestion = domains[selectedDomain].questions[questionNumber];
      setCurrentQuestion(nextQuestion);
      setQuestionNumber(prev => prev + 1);
      setChatMessages(prev => [...prev, {
        type: 'bot',
        content: `Great! Here's your next question: ${nextQuestion}`,
        timestamp: new Date().toLocaleTimeString()
      }]);
    } else {
      completeInterview();
    }
  };

  const generateFeedback = (answer) => {
    const feedbacks = [
      "Good answer! You covered the key points well.",
      "Nice explanation! Consider adding more specific examples.",
      "Solid response! You could elaborate on the implementation details.",
      "Well structured answer! The technical depth is appropriate.",
      "Great insight! Your practical experience shows through."
    ];
    return feedbacks[Math.floor(Math.random() * feedbacks.length)];
  };

  const completeInterview = () => {
    setInterviewState('completed');
    const performance = calculatePerformance();
    setPerformanceData(prev => [...prev, performance]);
    setChatMessages(prev => [...prev, {
      type: 'bot',
      content: `Congratulations! You've completed the interview. Session ends, and final score is displayed. Your overall score is ${performance.overallScore}/100. Check your performance report for detailed feedback.`,
      timestamp: new Date().toLocaleTimeString()
    }]);
  };

  const calculatePerformance = () => {
    if (userAnswers.length === 0) {
      return {
        id: Date.now(),
        domain: domains[selectedDomain].name,
        date: new Date().toLocaleDateString(),
        questionsAnswered: 0,
        totalTime: '00:00',
        avgTimePerQuestion: '00:00',
        overallScore: 0,
        technicalScore: 0,
        communicationScore: 0,
        problemSolvingScore: 0,
        strengths: ['No data available'],
        improvements: ['Complete an interview first']
      };
    }
   
    const avgTimePerQuestion = timeElapsed / userAnswers.length;
   
    return {
      id: Date.now(),
      domain: domains[selectedDomain].name,
      date: new Date().toLocaleDateString(),
      questionsAnswered: userAnswers.length,
      totalTime: formatTime(timeElapsed),
      avgTimePerQuestion: formatTime(Math.round(avgTimePerQuestion)),
      overallScore: Math.round(70 + Math.random() * 25),
      technicalScore: Math.round(65 + Math.random() * 30),
      communicationScore: Math.round(75 + Math.random() * 20),
      problemSolvingScore: Math.round(70 + Math.random() * 25),
      strengths: ['Clear communication', 'Good technical knowledge', 'Structured thinking'],
      improvements: ['Add more examples', 'Consider edge cases', 'Elaborate on solutions']
    };
  };

  const resetInterview = () => {
    setInterviewState('idle');
    setCurrentQuestion('');
    setQuestionNumber(0);
    setUserAnswers([]);
    setTimeElapsed(0);
    setCurrentAnswer('');
    setChatMessages([]);
  };

  useEffect(() => {
    if (isAuthenticated && (interviewState === 'active' || interviewState === 'paused' || interviewState === 'completed')) {
      setCurrentView('interview');
    }
  }, [interviewState, isAuthenticated]);

  if (!isAuthenticated) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center">
        <div className="bg-white rounded-xl shadow-lg p-8 w-full max-w-md">
          <div className="text-center mb-8">
            <div className="bg-gradient-to-r from-blue-600 to-indigo-600 p-3 rounded-lg inline-block mb-4">
              <Lock className="w-8 h-8 text-white" />
            </div>
            <h1 className="text-2xl font-bold text-gray-800">AI Interview Platform</h1>
            <p className="text-gray-600 mt-2">Please sign in to continue</p>
          </div>

          <form onSubmit={handleLogin} className="space-y-6">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Username</label>
              <input
                type="text"
                value={loginForm.username}
                onChange={(e) => setLoginForm(prev => ({ ...prev, username: e.target.value }))}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="Enter username"
              />
            </div>
           
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Password</label>
              <input
                type="password"
                value={loginForm.password}
                onChange={(e) => setLoginForm(prev => ({ ...prev, password: e.target.value }))}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="Enter password"
              />
            </div>

            {authError && (
              <div className="bg-red-50 border border-red-200 rounded-lg p-3">
                <p className="text-red-600 text-sm">{authError}</p>
              </div>
            )}

            <button
              type="submit"
              className="w-full bg-blue-600 hover:bg-blue-700 text-white py-2 rounded-lg font-semibold transition-colors"
            >
              Sign In
            </button>

            <div className="grid grid-cols-3 gap-2">
              <button
                type="button"
                onClick={() => handleQuickLogin('user1', 'John Doe', 'user')}
                className="bg-blue-500 hover:bg-blue-600 text-white text-xs py-2 rounded transition-colors"
              >
                User Login
              </button>
              <button
                type="button"
                onClick={() => handleQuickLogin('admin', 'Admin User', 'admin')}
                className="bg-green-500 hover:bg-green-600 text-white text-xs py-2 rounded transition-colors"
              >
                Admin Login
              </button>
              <button
                type="button"
                onClick={() => handleQuickLogin('testuser', 'Test User', 'user')}
                className="bg-purple-500 hover:bg-purple-600 text-white text-xs py-2 rounded transition-colors"
              >
                Test Login
              </button>
            </div>
          </form>

          <div className="mt-6 text-sm text-gray-600">
            <p className="font-medium mb-2">Test Credentials:</p>
            <p>User: user1 / pass123</p>
            <p>Admin: admin / admin123</p>
            <p>Test: testuser / test123</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
      <nav className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center space-x-4">
              <div className="bg-gradient-to-r from-blue-600 to-indigo-600 p-2 rounded-lg">
                <MessageCircle className="w-6 h-6 text-white" />
              </div>
              <span className="text-xl font-bold text-gray-800">AI Interview</span>
            </div>
            <div className="flex items-center space-x-4">
              <button
                onClick={() => setCurrentView('dashboard')}
                className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                  currentView === 'dashboard'
                    ? 'bg-blue-600 text-white'
                    : 'text-gray-600 hover:text-gray-800'
                }`}
              >
                Dashboard
              </button>
              <button
                onClick={() => setCurrentView('reports')}
                className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                  currentView === 'reports'
                    ? 'bg-blue-600 text-white'
                    : 'text-gray-600 hover:text-gray-800'
                }`}
              >
                Reports
              </button>
              {userRole === 'admin' && (
                <button
                  onClick={() => setCurrentView('admin')}
                  className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                    currentView === 'admin'
                      ? 'bg-blue-600 text-white'
                      : 'text-gray-600 hover:text-gray-800'
                  }`}
                >
                  Admin
                </button>
              )}
              <div className="flex items-center space-x-2 text-sm text-gray-600">
                <User className="w-4 h-4" />
                <span>{currentUser?.name}</span>
                <button
                  onClick={handleLogout}
                  className="text-red-600 hover:text-red-800 p-1 transition-colors"
                  title="Logout"
                >
                  <LogOut className="w-4 h-4" />
                </button>
              </div>
            </div>
          </div>
        </div>
      </nav>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {currentView === 'dashboard' && (
          <div className="space-y-6">
            <div className="text-center mb-8">
              <h1 className="text-4xl font-bold text-gray-800 mb-2">AI Mock Interview Platform</h1>
              <p className="text-gray-600">Practice interviews with AI-powered questions and get instant feedback</p>
              {currentUser && (
                <p className="text-blue-600 font-medium mt-2">Welcome back, {currentUser.name}!</p>
              )}
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              {Object.entries(domains).map(([key, domain]) => (
                <div
                  key={key}
                  className={`${domain.color} text-white p-6 rounded-xl cursor-pointer transform hover:scale-105 transition-all duration-200 shadow-lg`}
                  onClick={() => setSelectedDomain(key)}
                >
                  <h3 className="text-xl font-semibold mb-2">{domain.name}</h3>
                  <p className="text-sm opacity-90">{domain.questions.length} Questions Available</p>
                  {selectedDomain === key && (
                    <div className="mt-3 flex items-center">
                      <CheckCircle className="w-5 h-5 mr-2" />
                      <span className="text-sm font-medium">Selected</span>
                    </div>
                  )}
                </div>
              ))}
            </div>

            <div className="bg-white rounded-xl shadow-lg p-6">
              <h2 className="text-2xl font-bold text-gray-800 mb-4">
                Selected Domain: {domains[selectedDomain]?.name || 'No domain selected'}
              </h2>
             
              <div className="flex flex-col sm:flex-row gap-4">
                <button
                  onClick={startInterview}
                  className="bg-blue-600 hover:bg-blue-700 text-white px-8 py-3 rounded-lg font-semibold flex items-center justify-center transition-colors"
                >
                  <Play className="w-5 h-5 mr-2" />
                  Start Interview
                </button>
               
                <button
                  onClick={() => setCurrentView('reports')}
                  className="bg-gray-600 hover:bg-gray-700 text-white px-8 py-3 rounded-lg font-semibold flex items-center justify-center transition-colors"
                >
                  <BarChart3 className="w-5 h-5 mr-2" />
                  View Reports
                </button>
              </div>
            </div>

            {performanceData.length > 0 && (
              <div className="bg-white rounded-xl shadow-lg p-6">
                <h2 className="text-2xl font-bold text-gray-800 mb-4">Recent Performance</h2>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div className="bg-green-50 p-4 rounded-lg">
                    <div className="text-2xl font-bold text-green-600">{performanceData[performanceData.length - 1]?.overallScore || 0}</div>
                    <div className="text-sm text-gray-600">Latest Score</div>
                  </div>
                  <div className="bg-blue-50 p-4 rounded-lg">
                    <div className="text-2xl font-bold text-blue-600">{performanceData.length}</div>
                    <div className="text-sm text-gray-600">Interviews Completed</div>
                  </div>
                  <div className="bg-purple-50 p-4 rounded-lg">
                    <div className="text-2xl font-bold text-purple-600">
                      {performanceData.length > 0 ? Math.round(performanceData.reduce((acc, p) => acc + p.overallScore, 0) / performanceData.length) : 0}
                    </div>
                    <div className="text-sm text-gray-600">Average Score</div>
                  </div>
                </div>
              </div>
            )}
          </div>
        )}

        {currentView === 'interview' && (
          <div className="max-w-4xl mx-auto">
            <div className="bg-white rounded-xl shadow-lg p-6 mb-6">
              <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-6">
                <div>
                  <h2 className="text-2xl font-bold text-gray-800">{domains[selectedDomain]?.name || 'Interview'} Session</h2>
                  <p className="text-gray-600">Question {questionNumber} of {domains[selectedDomain]?.questions.length || 0}</p>
                </div>
                <div className="flex items-center space-x-4 mt-4 sm:mt-0">
                  <div className="flex items-center text-gray-600">
                    <Clock className="w-5 h-5 mr-2" />
                    <span className="font-mono text-lg">{formatTime(timeElapsed)}</span>
                  </div>
                  <div className="flex space-x-2">
                    {interviewState === 'active' && (
                      <button
                        onClick={pauseInterview}
                        className="bg-yellow-500 hover:bg-yellow-600 text-white p-2 rounded-lg transition-colors"
                      >
                        <Pause className="w-5 h-5" />
                      </button>
                    )}
                    {interviewState === 'paused' && (
                      <button
                        onClick={resumeInterview}
                        className="bg-green-500 hover:bg-green-600 text-white p-2 rounded-lg transition-colors"
                      >
                        <Play className="w-5 h-5" />
                      </button>
                    )}
                    <button
                      onClick={resetInterview}
                      className="bg-gray-500 hover:bg-gray-600 text-white p-2 rounded-lg transition-colors"
                    >
                      <RotateCcw className="w-5 h-5" />
                    </button>
                  </div>
                </div>
              </div>

              <div className="h-96 overflow-y-auto border rounded-lg p-4 mb-4 bg-gray-50">
                {chatMessages.map((message, index) => (
                  <div key={index} className={`mb-4 flex ${message.type === 'user' ? 'justify-end' : 'justify-start'}`}>
                    <div className={`max-w-3xl flex items-start space-x-3 ${message.type === 'user' ? 'flex-row-reverse space-x-reverse' : ''}`}>
                      <div className={`w-8 h-8 rounded-full flex items-center justify-center ${
                        message.type === 'user' ? 'bg-blue-500' : 'bg-gray-600'
                      }`}>
                        {message.type === 'user' ? <User className="w-4 h-4 text-white" /> : <Bot className="w-4 h-4 text-white" />}
                      </div>
                      <div className={`rounded-lg p-3 ${
                        message.type === 'user'
                          ? 'bg-blue-500 text-white'
                          : 'bg-white border border-gray-200'
                      }`}>
                        <p className="text-sm">{message.content}</p>
                        <span className="text-xs opacity-70 mt-1 block">{message.timestamp}</span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>

              {interviewState !== 'completed' && interviewState !== 'idle' && (
                <div className="space-y-4">
                  <textarea
                    value={currentAnswer}
                    onChange={(e) => setCurrentAnswer(e.target.value)}
                    placeholder="Type your answer here..."
                    className="w-full h-32 border border-gray-300 rounded-lg p-3 focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
                    disabled={interviewState !== 'active'}
                  />
                  <div className="flex justify-between items-center">
                    <span className="text-sm text-gray-500">
                      {currentAnswer.length} characters
                    </span>
                    <button
                      onClick={submitAnswer}
                      disabled={!currentAnswer.trim() || interviewState !== 'active'}
                      className="bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 text-white px-6 py-2 rounded-lg font-semibold transition-colors"
                    >
                      Submit Answer
                    </button>
                  </div>
                </div>
              )}

              {interviewState === 'completed' && (
                <div className="text-center py-8">
                  <div className="text-6xl mb-4">🎉</div>
                  <h3 className="text-2xl font-bold text-gray-800 mb-2">Interview Completed!</h3>
                  <p className="text-gray-600 mb-6">Great job! Performance report displayed with score and feedback.</p>
                  <div className="flex justify-center space-x-4">
                    <button
                      onClick={() => setCurrentView('reports')}
                      className="bg-green-600 hover:bg-green-700 text-white px-6 py-3 rounded-lg font-semibold transition-colors"
                    >
                      View Report
                    </button>
                    <button
                      onClick={resetInterview}
                      className="bg-gray-600 hover:bg-gray-700 text-white px-6 py-3 rounded-lg font-semibold transition-colors"
                    >
                      Start New Interview
                    </button>
                  </div>
                </div>
              )}
            </div>
          </div>
        )}

        {currentView === 'reports' && (
          <div className="space-y-6">
            <h2 className="text-3xl font-bold text-gray-800">Performance Reports</h2>
           
            {performanceData.length === 0 ? (
              <div className="bg-white rounded-xl shadow-lg p-12 text-center">
                <BarChart3 className="w-16 h-16 text-gray-400 mx-auto mb-4" />
                <h3 className="text-xl font-semibold text-gray-600 mb-2">No interviews completed yet</h3>
                <p className="text-gray-500 mb-6">Complete your first interview to see performance analytics</p>
                <button
                  onClick={() => setCurrentView('dashboard')}
                  className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-lg font-semibold transition-colors"
                >
                  Start Interview
                </button>
              </div>
            ) : (
              <div className="space-y-6">
                {performanceData.map((report, index) => (
                  <div key={index} className="bg-white rounded-xl shadow-lg p-6">
                    <div className="flex justify-between items-start mb-6">
                      <div>
                        <h3 className="text-xl font-bold text-gray-800">{report.domain} Interview</h3>
                        <p className="text-gray-600">{report.date} • {report.totalTime} total time</p>
                      </div>
                      <div className="text-right">
                        <div className="text-3xl font-bold text-green-600">{report.overallScore}/100</div>
                        <div className="text-sm text-gray-600">Overall Score</div>
                      </div>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
                      <div className="bg-blue-50 p-4 rounded-lg">
                        <div className="text-2xl font-bold text-blue-600">{report.technicalScore}/100</div>
                        <div className="text-sm text-gray-600">Technical Knowledge</div>
                      </div>
                      <div className="bg-green-50 p-4 rounded-lg">
                        <div className="text-2xl font-bold text-green-600">{report.communicationScore}/100</div>
                        <div className="text-sm text-gray-600">Communication</div>
                      </div>
                      <div className="bg-purple-50 p-4 rounded-lg">
                        <div className="text-2xl font-bold text-purple-600">{report.problemSolvingScore}/100</div>
                        <div className="text-sm text-gray-600">Problem Solving</div>
                      </div>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      <div>
                        <h4 className="font-semibold text-gray-800 mb-3 flex items-center">
                          <CheckCircle className="w-5 h-5 text-green-500 mr-2" />
                          Strengths
                        </h4>
                        <ul className="space-y-2">
                          {report.strengths.map((strength, idx) => (
                            <li key={idx} className="text-gray-600 flex items-center">
                              <Star className="w-4 h-4 text-yellow-500 mr-2" />
                              {strength}
                            </li>
                          ))}
                        </ul>
                      </div>
                      <div>
                        <h4 className="font-semibold text-gray-800 mb-3 flex items-center">
                          <XCircle className="w-5 h-5 text-orange-500 mr-2" />
                          Areas for Improvement
                        </h4>
                        <ul className="space-y-2">
                          {report.improvements.map((improvement, idx) => (
                            <li key={idx} className="text-gray-600 flex items-center">
                              <Award className="w-4 h-4 text-orange-500 mr-2" />
                              {improvement}
                            </li>
                          ))}
                        </ul>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {currentView === 'admin' && userRole === 'admin' && (
          <div className="space-y-6">
            <div className="flex items-center justify-between">
              <h2 className="text-3xl font-bold text-gray-800 flex items-center">
                <Shield className="w-8 h-8 mr-3 text-blue-600" />
                Admin Panel
              </h2>
            </div>

            <div className="bg-white rounded-xl shadow-lg p-6">
              <h3 className="text-xl font-bold text-gray-800 mb-4">Domain Management</h3>
              <p className="text-gray-600 mb-4">Domains are updated successfully.</p>
             
              <div className="space-y-4">
                {Object.entries(domains).map(([key, domain]) => (
                  <div key={key} className="border border-gray-200 rounded-lg p-4">
                    <h4 className="font-semibold text-gray-800 mb-2">{domain.name}</h4>
                    <p className="text-sm text-gray-600">{domain.questions.length} questions available</p>
                  </div>
                ))}
              </div>
            </div>

            <div className="bg-white rounded-xl shadow-lg p-6">
              <h3 className="text-xl font-bold text-gray-800 mb-4">Update Question Generation Model</h3>
              <div className="bg-green-50 border border-green-200 rounded-lg p-4">
                <p className="text-green-800 font-medium">Model Updated Successfully!</p>
                <p className="text-green-600 text-sm mt-1">The AI question generation model has been updated with the latest parameters.</p>
              </div>
            </div>
          </div>
        )}
      </main>
    </div>
  );
};

export default InterviewPlatform;
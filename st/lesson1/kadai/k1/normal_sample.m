clear;

% x=-3:0.01:3; % particular values of a random variable X
x=-0.5:0.01:0.5;
% x=-0.2:0.01:0.2;
% x=-0.1:0.01:0.1;
myu=0;	  % the mean of the normal distribution
sigma2=1;  % the variance of the normal distribution
n=50; % 5 50 500 5000 

y=ndf(x,myu, sigma2/n); % normal density function

plot(x,y);   % plot the normal density function

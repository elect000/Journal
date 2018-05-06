clear;

P = 0.2;       
M = 500;      
N = 5000; % 5 50 500 5000        
for i=1:M
a = rand(1, N);
b = a<P;
c = sum(b);
av(i) = c / N;
end
x = 0:0.01:1;
hist(av, x);
% axis([0.0 1.0 0.0 200]);
% axis([0.0 0.4 0.0 200]);
axis([0.1 0.3 0.0 400]);

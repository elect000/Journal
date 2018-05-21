function [y]=ndf(x,myu,sigma2)
y=exp(-(x-myu).^2/(2*sigma2))/(sqrt(2*pi*sigma2));

import { HttpInterceptorFn } from '@angular/common/http';

export const credentialsInterceptor: HttpInterceptorFn = (req, next) => {
  // Adjunta obligatoriamente la bandera withCredentials en cada petición enviada al Gateway
  const clonedRequest = req.clone({
    withCredentials: true,
  });
  return next(clonedRequest);
};

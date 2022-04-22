package dk.sunepoulsen.tes.springboot.template.client.rs;

import dk.sunepoulsen.tes.rest.integrations.TechEasySolutionsBackendIntegrator;
import dk.sunepoulsen.tes.rest.integrations.TechEasySolutionsClient;
import dk.sunepoulsen.tes.rest.models.PaginationModel;
import dk.sunepoulsen.tes.springboot.template.client.rs.model.TemplateModel;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.subjects.CompletableSubject;
import org.springframework.data.domain.Pageable;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TemplateIntegrator extends TechEasySolutionsBackendIntegrator {
    public TemplateIntegrator(TechEasySolutionsClient httpClient) {
        super(httpClient);
    }

    public Single<TemplateModel> create(TemplateModel model) {
        return Single.fromFuture(httpClient.post("/templates", model, TemplateModel.class))
                .onErrorResumeNext(this::mapClientExceptions);
    }

    public Single<PaginationModel> findAll() {
        return findAll(null);
    }

    public Single<PaginationModel> findAll(Pageable pageable) {
        StringBuffer url = new StringBuffer();
        url.append("/templates");

        if (pageable != null && pageable.isPaged()) {
            url.append("?page=");
            url.append(pageable.getPageNumber());
            url.append("&size=");
            url.append(pageable.getPageSize());

            pageable.getSort().forEach(order -> {
                url.append("&sort=");
                url.append(URLEncoder.encode(order.getProperty(), StandardCharsets.UTF_8));
                if (order.isDescending()) {
                    url.append(",desc");
                }
            });
        }

        return Single.fromFuture(httpClient.get(url.toString(), PaginationModel.class))
            .onErrorResumeNext(this::mapClientExceptions);
    }

    public Single<TemplateModel> get(Long id) {
        return Single.fromFuture(httpClient.get("/templates/" + id.toString(), TemplateModel.class))
            .onErrorResumeNext(this::mapClientExceptions);
    }

    public Single<TemplateModel> patch(Long id, TemplateModel model) {
        return Single.fromFuture(httpClient.patch("/templates/" + id.toString(), model, TemplateModel.class))
            .onErrorResumeNext(this::mapClientExceptions);
    }

    public Completable delete(Long id) {
        return CompletableSubject.fromFuture(httpClient.delete("/templates/" + id.toString()))
            .onErrorResumeNext(this::mapClientExceptionsAsCompletable);
    }
}

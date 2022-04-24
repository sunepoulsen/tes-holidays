package dk.sunepoulsen.tes.holidays.client.rs;

import dk.sunepoulsen.tes.holidays.client.rs.model.HolidayModel;
import dk.sunepoulsen.tes.rest.integrations.TechEasySolutionsBackendIntegrator;
import dk.sunepoulsen.tes.rest.integrations.TechEasySolutionsClient;
import dk.sunepoulsen.tes.rest.models.PaginationModel;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.subjects.CompletableSubject;
import org.springframework.data.domain.Pageable;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class HolidayIntegrator extends TechEasySolutionsBackendIntegrator {
    static final String BASE_ENDPOINT_PATH = "/holidays";

    public HolidayIntegrator(TechEasySolutionsClient httpClient) {
        super(httpClient);
    }

    public Single<HolidayModel> create(HolidayModel model) {
        return Single.fromFuture(httpClient.post(BASE_ENDPOINT_PATH, model, HolidayModel.class))
                .onErrorResumeNext(this::mapClientExceptions);
    }

    public Single<PaginationModel> findAll() {
        return findAll(null);
    }

    public Single<PaginationModel> findAll(Pageable pageable) {
        StringBuffer url = new StringBuffer();
        url.append(BASE_ENDPOINT_PATH);

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

    public Single<HolidayModel> get(Long id) {
        return Single.fromFuture(httpClient.get(BASE_ENDPOINT_PATH + "/" + id.toString(), HolidayModel.class))
            .onErrorResumeNext(this::mapClientExceptions);
    }

    public Single<HolidayModel> patch(Long id, HolidayModel model) {
        return Single.fromFuture(httpClient.patch(BASE_ENDPOINT_PATH + "/" + id.toString(), model, HolidayModel.class))
            .onErrorResumeNext(this::mapClientExceptions);
    }

    public Completable delete(Long id) {
        return CompletableSubject.fromFuture(httpClient.delete(BASE_ENDPOINT_PATH + "/" + id.toString()))
            .onErrorResumeNext(this::mapClientExceptionsAsCompletable);
    }
}

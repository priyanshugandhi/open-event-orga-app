package org.fossasia.openevent.app.core.session.create;

import org.fossasia.openevent.app.common.mvp.presenter.AbstractBasePresenter;
import org.fossasia.openevent.app.common.rx.Logger;
import org.fossasia.openevent.app.data.event.Event;
import org.fossasia.openevent.app.data.session.Session;
import org.fossasia.openevent.app.data.session.SessionRepository;
import org.fossasia.openevent.app.data.tracks.Track;
import org.fossasia.openevent.app.utils.DateUtils;
import org.fossasia.openevent.app.utils.StringUtils;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeParseException;

import javax.inject.Inject;

import static org.fossasia.openevent.app.common.rx.ViewTransformers.dispose;
import static org.fossasia.openevent.app.common.rx.ViewTransformers.progressiveErroneous;

public class CreateSessionPresenter extends AbstractBasePresenter<CreateSessionView> {

    private final SessionRepository sessionRepository;
    private final Session session = new Session();

    @Inject
    public CreateSessionPresenter(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;

        LocalDateTime current = LocalDateTime.now();

        String isoDate = DateUtils.formatDateToIso(current);
        session.setStartsAt(isoDate);
        session.setEndsAt(isoDate);
    }

    @Override
    public void start() {
        // Nothing to do
    }

    public Session getSession() {
        return session;
    }

    private boolean verify() {
        try {
            ZonedDateTime start = DateUtils.getDate(session.getStartsAt());
            ZonedDateTime end = DateUtils.getDate(session.getEndsAt());

            if (!end.isAfter(start)) {
                getView().showError("End time should be after start time");
                return false;
            }
            return true;
        } catch (DateTimeParseException pe) {
            getView().showError("Please enter date in correct format");
            return false;
        }
    }

    protected void nullifyEmptyFields(Session session) {
        session.setSlidesUrl(StringUtils.emptyToNull(session.getSlidesUrl()));
        session.setAudioUrl(StringUtils.emptyToNull(session.getAudioUrl()));
        session.setVideoUrl(StringUtils.emptyToNull(session.getVideoUrl()));
        session.setSignupUrl(StringUtils.emptyToNull(session.getSignupUrl()));
    }

    public void createSession(long trackId, long eventId) {
        if (!verify())
            return;

        Track track = new Track();
        Event event = new Event();

        track.setId(trackId);
        event.setId(eventId);
        session.setTrack(track);
        session.setEvent(event);

        nullifyEmptyFields(session);

        sessionRepository
            .createSession(session)
            .compose(dispose(getDisposable()))
            .compose(progressiveErroneous(getView()))
            .subscribe(createdSession -> getView().onSuccess("Session Created"), Logger::logError);
    }
}

from sqlalchemy import func
from app.core.database import db
from app.models import Rating


class RatingRepository:

    @staticmethod
    def get_by_ratee(ratee_id: str):
        return Rating.query.filter_by(ratee_id=ratee_id).order_by(Rating.created_at.desc()).all()

    @staticmethod
    def find_existing(transaction_id: str, rater_id: str) -> Rating | None:
        return Rating.query.filter_by(
            transaction_id=transaction_id,
            rater_id=rater_id
        ).first()

    @staticmethod
    def create(transaction_id: str, rater_id: str, ratee_id: str,
               score: int, comment: str | None = None) -> Rating:
        rating = Rating(
            transaction_id=transaction_id,
            rater_id=rater_id,
            ratee_id=ratee_id,
            score=score,
            comment=comment,
        )
        db.session.add(rating)
        return rating

    @staticmethod
    def average_for_user(ratee_id: str) -> float | None:
        return db.session.query(func.avg(Rating.score)).filter_by(ratee_id=ratee_id).scalar()

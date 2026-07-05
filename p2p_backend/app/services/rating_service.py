from app.core.database import db
from app.core.exceptions import NotFoundError, AuthorizationError, AppException, ConflictError
from app.models import Rating
from app.repositories.rating_repository import RatingRepository
from app.repositories.transaction_repository import TransactionRepository
from app.repositories.user_repository import UserRepository


class RatingService:

    @staticmethod
    def get_received(user_id: str) -> dict:
        ratings = RatingRepository.get_by_ratee(user_id)
        raters = {
            u.id: u for u in
            UserRepository.get_by_ids([r.rater_id for r in ratings])
        }
        result = []
        for r in ratings:
            rater = raters.get(r.rater_id)
            result.append({
                'id': str(r.id),
                'score': r.score,
                'comment': r.comment,
                'rater_name': rater.full_name if rater else 'Anónimo',
                'created_at': r.created_at.isoformat() if r.created_at else None,
            })

        total = len(result)
        avg = sum(x['score'] for x in result) / total if total > 0 else 0.0
        dist = {i: sum(1 for x in result if x['score'] == i) for i in range(1, 6)}

        return {
            'ratings': result,
            'average': round(avg, 2),
            'total': total,
            'distribution': dist,
        }

    @staticmethod
    def create(user_id: str, data: dict) -> dict:
        txn = TransactionRepository.get_by_id(data.get('transaction_id'))
        if not txn:
            raise NotFoundError('Transaction not found')
        if txn.status not in ('completed', 'closed'):
            raise AppException('INVALID_STATE', 'Can only rate completed transactions', 400)
        if txn.buyer_id != user_id and txn.vendor_id != user_id:
            raise AuthorizationError('Not your transaction')

        existing = RatingRepository.find_existing(txn.id, user_id)
        if existing:
            raise ConflictError('Already rated this transaction')

        score = data.get('score', 5)
        if not isinstance(score, int) or not (1 <= score <= 5):
            raise AppException('INVALID_SCORE', 'Score must be 1-5', 400)

        ratee_id = txn.vendor_id if txn.buyer_id == user_id else txn.buyer_id

        rating = RatingRepository.create(
            transaction_id=txn.id,
            rater_id=user_id,
            ratee_id=ratee_id,
            score=score,
            comment=data.get('comment'),
        )

        ratee = UserRepository.get_by_id(ratee_id)
        if ratee:
            avg = RatingRepository.average_for_user(ratee_id)
            ratee.rating = round(float(avg if avg is not None else score), 2)

        if txn.buyer_id == user_id and txn.status == 'completed':
            txn.status = 'closed'

        db.session.commit()
        return {'id': rating.id, 'score': rating.score, 'message': 'Rating submitted'}

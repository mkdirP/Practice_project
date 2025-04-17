from collections import defaultdict
import json


class ValidationModels:
    class ValidationMessage:
        def __init__(self, code, message, suggestion, content):
            self.code = code
            self.message = message
            self.suggestion = suggestion
            self.content = content

        def to_dict(self):
            return self.__dict__

    class ValidationStats:
        def __init__(self):
            self.total_paragraphs = 0
            self.total_errors = 0
            self.error_type_count = defaultdict(int)

        def to_dict(self):
            return {
                "totalParagraphs": self.total_paragraphs,
                "totalErrors": self.total_errors,
                "errorTypeCount": dict(self.error_type_count)
            }

    class ValidationResponse:
        def __init__(self, messages, stats):
            self.messages = [m.to_dict() for m in messages]
            self.stats = stats.to_dict()

        def to_json(self):
            return json.dumps(self.__dict__, ensure_ascii=False, indent=2)

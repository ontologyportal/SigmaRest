import math
import requests
from requests import Response


class SumoAPI:
    def __init__(self, url: str = "http://localhost:8080/sigmarest/resources"):
        self.url: str = url

    def init(self) -> bool:
        try:
            self._get('init')
        except:
            return False
        return True

    def reset(self) -> bool:
        try:
            self._get('reset')
        except:
            return False
        return True

    def tell(self, statement: str) -> bool:
        try:
            self._get('tell', {'statement': statement})
        except:
            return False
        return True

    def tell_all(self, statements: list[str]) -> bool:
        success = True
        for statement in statements:
            success &= self.tell(statement)
        return success

    def ask(self, query: str, timeout: int =10) -> dict[str, str]:
        resp = self._get('ask', {'query': query, 'timeout': timeout})
        return resp.json()

    def ask_max(self, query: str, timeout: int = 10, max_answers: int = 2) -> list[dict[str, str]]:
        ret_hash = []
        if max_answers < 0:
            max_answers = math.inf
        while len(ret_hash) < max_answers:
            resp = None
            resp = self._get('ask', {'query': query, 'timeout': timeout}).json()
            if 'error' in resp:
                break
            bindings = resp['bindings']
            ret_hash.append(bindings)
            new_str = '(and %s' % query
            for free_var in bindings:
                new_str += ' (not (equal %s %s))' % (free_var, bindings[free_var])
            new_str += ')'
            query = new_str
            new_str = ''
        return ret_hash

    def _get(self, endpoint: str, params: dict | None = None) -> Response:
        if params != ():
            return requests.get(f"{self.url}/{endpoint}", params=params)
        return requests.get(f"{self.url}/{endpoint}")

    def _post(self, endpoint: str, payload: dict | list) -> Response:
        headers = {"Content-Type": "application/json"}
        return requests.post(f"{self.url}/{endpoint}", json=payload, headers=headers)

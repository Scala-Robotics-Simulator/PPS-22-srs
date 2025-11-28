def rename_inner_keys(data_dict: dict, mapping: dict) -> dict:
    """
    Modifies data_dict in-place to rename keys based on mapping.
    """
    for sub_dict in data_dict.values():
        for old_key, new_key in mapping.items():
            if old_key in sub_dict:
                sub_dict[new_key] = sub_dict.pop(old_key)
    return data_dict


def combine_inner_dict(dict1: dict, dict2: dict) -> dict:
    result = {}
    for k, v in dict1.items():
        result[k] = v | dict2[k]
    return result

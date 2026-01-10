export enum ErrorType {
    NotAo3Link = 'NotAo3Link',
    RestrictedWork = 'RestrictedWork',
}

export interface RecoverableError {
    errorType: ErrorType;
    defaultMessage: string;
    additionalData: any;
}

export async function asRecoverableError(resp: Response, expectedTypes: ErrorType[]): Promise<RecoverableError | null> {
    if (resp.headers.get('content-type') !== 'application/json') {
        return null;
    }
    const errorObj = await resp.json();
    for (const errorType of expectedTypes) {
        const data = errorObj[errorType];
        if (data) {
            return toError(data, errorType);
        }
    }
    return null;
}

function toError(data: any, errorType: ErrorType): RecoverableError {
    switch (errorType) {
        case ErrorType.NotAo3Link: {
            const link = data['link'];
            return { errorType, defaultMessage: `'${link}' is not a parsable Ao3 link`, additionalData: link };
        }
        case ErrorType.RestrictedWork: {
            const entityName = data['entityName'];
            return {
                errorType,
                defaultMessage: 'This work is restricted. To parse it please use its HTML download link',
                additionalData: entityName,
            };
        }
        default: {
            return { errorType, defaultMessage: '', additionalData: data };
        }
    }
}
